## Quick Links

- [Plankton Metadata](metadata.md)
- [Feature Research](feature-research.md)

--------------------

# Lab Notebook

**4/25/2015**

Investigated data: [data about data](metadata.md)

Many classes seem under-represented. Of the 121 classes, half of them have <115 records in that class. These are going to be more difficult to predict, unless they have very little variance between images (unlikely). And then there are 4 labels with over 1000 records. Should these be down-sampled? Probably not, already not very much data present.

[This histogram](imgs/plankton_classes_counts.png) is a nice visualization of the size of each class and how that contributes to the overall training data. It makes it fairly clear that half of the data comes from classes with ~500 records or fewer. And about a fifth of it comes from classes with more than 1000 records.

[Image sizes](imgs/image_sizes.png) are also much less consistent than I thought. Will need to look into how to handle that.

--------------------

**4/26/2015**

Feature Ideas:
- Histogram of pixel values
- Bucketed histogram of pixel values (reduce feature space)
  - scale number and size of buckets with distribution of pixel values?
- Pixel values average over scaled/sliding windows of the image
  - Ex: Average pixel value for each quadrant
- Keypoint extractors
  - SURF
  - SIFT
  - ORB
  - Requires some additional work after keypoint descriptors are found. Must cluster them so that their presence/absence can be converted into a vector (and the vector isn't huge).

Looking into expanding the training data via data augmentation: adding noise, blurring, transformations, etc.

Is normalizing the images a necessity? Probably.

Next steps are to get an end-to-end pipeline working., even if it's just assigning random values.

--------------------

**5/2/2015**

Feature Ideas:
- Original dimensions of the image
  - maybe summed?

Finally got everything to just work. Spark does not play nice with pretty much any image analysis library due to the requirement that *everything* has to be serializable so that it can be shipped from the master node to the workers. Furthermore, extending Spark ml's transformers and estimators looks daunting as it isn't particularly well-documented as to the purpose of each of the 4 mandatory methods. Ended up just getting everything to work by jamming the data into custom objects and keeping those in an RDD (why did they make DataFrames so difficult to work with?).

Had to create a quick and dirty pipeline abstraction since Spark ml's wasn't exatly cooperating. Everything runs successfully, now to do some feature engineering...

Questions for Ravi:
- Spark ml vs mllib?
- Data normalization
- Data augmentation
- Image feature extraction in Spark?

--------------------

**5/3/2015**

From the call today:
- Spark ml vs mllib
  - ml is new pipeline-focused API. Very powerful, not all of the algorithms/models in there yet.
  - mllib is the old API that Spark is moving away from. Has more algorithms/models implemented though.
- Ideas for data normalization:
  - Threshold each image according to the same criteria
  - 'Weber'
  - Scale each image to be the same size (use bicubic interpolation)
    (Internet seems to think that ~96 pixels on each edge works well)
- Ideas for data augmentaiton:
  - Flip image
  - Rotate?
  - Mirror image
  - Invert color/greyscale
  - Zoom?
- Don't need to do image feature extraction in Spark! Too difficult to get everything to place nice. Just do it locally and use spark for loading the processed training data and building the models

--------------------

**5/5/2015**

Removing Spark-related feature extraction code and transitioning prototyped pipeline framework into something more usable (and using Java 8 Streams!). Reintegrating OpenCV (java wrapper). Creating fundamental data structures needed to store features.

--------------------

**5/6/2015**

Finished design and majority of implementation of feature extraction pipeline framework.

Implemented features (all under `collinm.framework.features`):
- Image Histogram
- Dimensions
- Pixel Count

Image histogram provides a vector of 255 values enumerating how many of each pixel value was present in the image. It's an obvious and probably not terribly useful feature, but it's a good baseline to start with. Obvious caveat is that different image sizes will have different numbers of pixels and will ultimately confuse the models. Underscores the need to normalize the images...

Dimensions provides a vector of 2 values: height and width (in pixels). Simple feature to indicate size and shape. Of course, it's *not* rotation invariant, but that can be handled with data augmentation later.

Pixel count provides a vector of 1 value (kept as a vector for consistency with other features): the total number of pixels in the image. This might end up being a better signal for image size than the dimension features above.

Added a JSON DataSink (output) so that I can finally train and evaluate something in Spark. Next step is do the first experiment in Spark!

--------------------

**5/7/2015**

- Serialization and deserialization with JSON are working smoothly (Record object -> JSON -> LabeledPoint). This should be the last functional item required before running a feature extraction pipeline.
- Removed all `println`'s and replaced them with proper logging (SLF4J + logback).

Refactored the package structure of the project to give everything better logical grouping:
- `collinm.framework`: feature extraction framework componenets
- `collinm.framework.data`: Runtime data objects
- `collinm.framework.io`: Input/Output (sources and sinks)
- `collinm.framework.json`: JSON serialization and deserialization
- `collinm.plankton.training`: feature experiments
- `collinm.plankton.testing`: Code to train and evaluate models in Spark

Ran the first feature extraction pipeline!

**Experiment 1**: `collinm.plankton.training.Experiment1.java`  
Features:
- histogram of pixel values (0-255)
Results/Metrics (single run, random 80:20 split):
- Precision = 0.2126
- Recall = 0.2126
- F1 = 0.2126

NOTE: feature extraction pipelines must be run with `-Djava.library.path=lib/x64/` or some variation thereof pointing to the directory that holds the compiled OpenCV binary for the runtime platform. Excluding this argument will cause the job to fail!

Questions:
- How does Spark know which attribute of the Java class to predict? Is it as simple as the one named 'label'?
- spark.ml logistic regression is limited to binary classification...?

--------------------

**5/8/2015**

- Standard Spark way of measuring performance metrics is too strict, so I implemented my own: `collinm.plankton.testing.ConfusionMatrix`. It doesn't work with RDDs, but the performance data is small enought that it doesn't matter.
- Refactored some IO code to make it more reusable, namely reading in the processed training data.
- Added functionality to enable stratified sampling and thus cross validation.
- Generalized Logistic Regression Runner so that input, output, and k values are *not* hard-coded. Now works properly with `spark-submit`
- Added image size normalization functionality

Continuation of experiment 1: 5-fold cross-validation of logistic regression with histogram features (see yesterday for feature details). (Full results)[results/experiment1.csv]
- Average Precision = 0.1876
- Average Recall = 0.2114
- Average F1 = 0.1988

**Experiment 2**: `collinmc.plankton.training.Experiment2.java`  
Features:
- histogram of pixel values (0-255)
- dimensions of image (height, width)
Results of 5-fold logistic regression ((Full results)[results/experiment2.csv]):
- Average Precision = 0.2620
- Average Recall = 0.3055
- Average F1 = 0.2821
Comments: 5% increase on precision, 9% increase on recall, and 7% incraese on F1. Dimension of the image clearly carries a lot of signal.

**Experiment 3**: `collinmc.plankton.training.Experiment3.java`  
Features:
- histogram of pixel values (0-255)
- dimensions of image (height, width)
- pixel count
Results of 5-fold logistic regression ((Full results)[results/experiment3.csv]):
- Average Precision = 0.2682
- Average Recall = 0.3108
- Average F1 = 0.2879
Comments: 1% increase on recall, but otherwise negligible improvements. Pixel count probably provides much of the same signal as the dimensions features; however, the small performance increase might be due to generalizing/decoupling absolute size from explicit image ratio as encoded by image dimensions. I'll keep the feature for now, as it doesn't seem to be noisy and it helps with recall a little. It's also worth noting that in terms of objective value pixel count is waaaay bigger than the others. Will this cause scaling problems? Should I be normalizing feature values?

**Experiment 4**: `collinmc.plankton.training.Experiment4.java`  
Features:
- dimensions of image (height, width)
- pixel count
- Normalize image size to 128x128
- histogram of pixel values (0-255)
Results of 5-fold logistic regression ((Full results)[results/experiment4.csv]):
- Average Precision = 0.3169
- Average Recall = 0.3590
- Average F1 = 0.3366
Comments: ~5% increase on precision, ~5% increase on recall, and ~5% increase on F1! Normalizing the inputs would seem to help substantially. It's worth noting here that the normalization routine scales the image 1:1 up/down to the desired max edge length based on the longest dimension. After the scaling is done, the shorter dimension is padded with white pixels (255, 255, 255) to achieve a square image. I've explicitly avoided scaling each dimension at different rates as it seems like it would throw out signal, especially considering that the resulting image would have many more interpolated pixel values.

For the sake of convenience, here's the command for running cross-validated logistic regression from the command line (working directory = project directory): `spark-submit --class collinm.plankton.testing.LogisticRegressionRunner --master local[7] build\libs\plankton-0-fat.jar output\experiment<num>.json doc\results\experiment<num>.csv <num-folds>`

Questions:
- Is there any value in normalizing feature vector values? That is, are various algorithms sensitive to the absolute size of an individual feature?
