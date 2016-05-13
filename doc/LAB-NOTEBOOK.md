## Quick Links

- [Plankton Metadata](metadata.md)
- [Feature Research](feature-research.md)

--------------------

# Lab Notebook

**4/25/2016**

Investigated data: [data about data](metadata.md)

Many classes seem under-represented. Of the 121 classes, half of them have <115 records in that class. These are going to be more difficult to predict, unless they have very little variance between images (unlikely). And then there are 4 labels with over 1000 records. Should these be down-sampled? Probably not, already not very much data present.

[This histogram](imgs/plankton_classes_counts.png) is a nice visualization of the size of each class and how that contributes to the overall training data. It makes it fairly clear that half of the data comes from classes with ~500 records or fewer. And about a fifth of it comes from classes with more than 1000 records.

[Image sizes](imgs/image_sizes.png) are also much less consistent than I thought. Will need to look into how to handle that.

--------------------

**4/26/2016**

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

**5/2/2016**

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

**5/3/2016**

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

**5/5/2016**

Removing Spark-related feature extraction code and transitioning prototyped pipeline framework into something more usable (and using Java 8 Streams!). Reintegrating OpenCV (java wrapper). Creating fundamental data structures needed to store features.

--------------------

**5/6/2016**

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

**5/7/2016**

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

**Experiment 1 - Logistic Regression**: `collinm.plankton.training.Experiment1.java`  
Motivation/Hypothesis: Establish a baseline feature set and performance metrics.

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

**5/8/2016**

- Standard Spark way of measuring performance metrics is too strict, so I implemented my own: `collinm.plankton.testing.ConfusionMatrix`. It doesn't work with RDDs, but the performance data is small enought that it doesn't matter.
- Refactored some IO code to make it more reusable, namely reading in the processed training data.
- Added functionality to enable stratified sampling and thus cross validation.
- Generalized Logistic Regression Runner so that input, output, and k values are *not* hard-coded. Now works properly with `spark-submit`
- Added image size normalization functionality

Continuation of experiment 1: 5-fold cross-validation of logistic regression with histogram features (see yesterday for feature details). [Full results](results/experiment1.csv)
- Average Precision = 0.1876
- Average Recall = 0.2114
- Average F1 = 0.1988

**Experiment 2 - Logistic Regression**: `collinmc.plankton.training.Experiment2.java`  
Motivation/Hypothesis: Do the dimensions of the image carry any signal?

Features:
- histogram of pixel values (0-255)
- dimensions of image (height, width)

Results of 5-fold logistic regression ([Full results](results/experiment2.csv)):
- Average Precision = 0.2620
- Average Recall = 0.3055
- Average F1 = 0.2821

Comments: 5% increase on precision, 9% increase on recall, and 7% incraese on F1. Dimension of the image clearly carries a lot of signal.

**Experiment 3 - Logistic Regression**: `collinmc.plankton.training.Experiment3.java`  
Motivation/Hypothesis: Does pixel count carry any additional signal that dimensions does not?

Features:
- histogram of pixel values (0-255)
- dimensions of image (height, width)
- pixel count

Results of 5-fold logistic regression ([Full results](results/experiment3.csv)):
- Average Precision = 0.2682
- Average Recall = 0.3108
- Average F1 = 0.2879

Comments: 1% increase on recall, but otherwise negligible improvements. Pixel count probably provides much of the same signal as the dimensions features; however, the small performance increase might be due to generalizing/decoupling absolute size from explicit image ratio as encoded by image dimensions. I'll keep the feature for now, as it doesn't seem to be noisy and it helps with recall a little. It's also worth noting that in terms of objective value pixel count is waaaay bigger than the others. Will this cause scaling problems? Should I be normalizing feature values?

**Experiment 4 - Logistic Regression**: `collinmc.plankton.training.Experiment4.java`  
Motivation/Hypothesis: Will normalizing the image size (without skewing due to scaling) impact the signal contained in the histogram by controlling the total number of pixels?

Features:
- dimensions of image (height, width)
- pixel count
- normalize image size to 128x128
- histogram of pixel values (0-255)

Results of 5-fold logistic regression ([Full results](results/experiment4.csv)):
- Average Precision = 0.3169
- Average Recall = 0.3590
- Average F1 = 0.3366

Comments: ~5% increase on precision, ~5% increase on recall, and ~5% increase on F1! Normalizing the inputs would seem to help substantially. It's worth noting here that the normalization routine scales the image 1:1 up/down to the desired max edge length based on the longest dimension. After the scaling is done, the shorter dimension is padded with white pixels (255, 255, 255) to achieve a square image. I've explicitly avoided scaling each dimension at different rates as it seems like it would throw out signal, especially considering that the resulting image would have many more interpolated pixel values.

For the sake of convenience, here's the command for running cross-validated logistic regression from the command line (working directory = project directory): `spark-submit --class collinm.plankton.testing.LogisticRegressionRunner --master local[7] build\libs\plankton-0-fat.jar output\experiment<num>.json doc\results\experiment<num>.csv <num-folds>`

Questions:
- Is there any value in normalizing feature vector values? That is, are various algorithms sensitive to the absolute size of an individual feature?

--------------------

**5/9/2016**

Added new feature: sub-region average. This feature scans through the image matrix with a custom size window (always square) taking the average of all the pixel values in each window and adding said values as a feature. The window scans in steps the size of the window, unless an overlap is specified then it steps in `window size - overlap`. This features seems like a relatively basic way to encode structural information about the image. Up to this point, none of the features have taken the positions of any pixels, even in aggregate, into account.

Added new augmentation: flipping images horizontally and vertically. I wrote some code to create a copy of an image and flip it vertically (over the X axis) or horizontally (over the Y axis). Any instance of this object will only create one additional image (double the data), but chaining them together can allow us to compound the changes and data multiplication.

**Experiment 5 - Logistic Regression**: `collinmc.plankton.training.Experiment5.java`  
Motivation/Hypothesis: Assuming that sub-region averages are providing the structural signal that I think they are, do they improve performance with logistic regression?

Features:
- dimensions of image (height, width)
- pixel count
- normalize image size to 128x128
- histogram of pixel values (0-255)
- sub-region averaging (8x8, 0 overlap)

Results of 5-fold logistic regression ([Full results](results/experiment5-lr/metrics.csv)):
- Average Precision = 0.2484
- Average Recall = 0.3205
- Average F1 = 0.2798

Comments: Overall, performance decreased 3-6% across all of the metrics. Taking a look at the confusion matrices, the models seem to be overfitting on 7 of the 121 classes:
- acantharia_protist (index=2)
- chaetognath_other (index=11)
- copepod_cyclopoid_oithona_eggs (index=24)
- protist_other (index=85)
- trichodesmium_bowtie (index=108)
- trichodesmium_multiple (index=109)
- trichodesmium_puff (index=110)

Comparing the confusion matrices for this experiment to those of experiment 4, this experiment's are much more concentrated on the above 7 classes to the exclusion of most other classes.

The addition of the sub-region average features roughlty tripled the size of the training data. While in and of itself this is probably not a problem, I *suspect* that the structural nature of the sub-region features made it such that there is no longer a single, linear decision boundary; thus, logistic regression is having a hard time making good decisions. I think I'll need to try a non-linear model next, possibly random forests...

The parameters for the sub-regions could be non-optimal as well. I'll need to test different values for window size and overlap to see if it makes any difference. Decreasing window size and/or increasing overlap will have a respectable impact on the number of features though.

Lastly, the structural features might just be under-represented due to the greater variation that's possible in a two-dimensional image. Data augmentation might be able to help here.

**Experiment 6**: `collinmc.plankton.training.Experiment6.java`  
Motivation/Hypothesis: Will data augmentation help structural features elucidate better signal?

Features:
- dimensions of image (height, width)
- pixel count
- normalize image size to 128x128
- histogram of pixel values (0-255)
- augment data by flipping image vertically (2x data)
- augment data by flipping image horizontally (2x data)
- sub-region averaging (8x8, 0 overlap)

Results of 5-fold logistic regression ([Full results](results/experiment6-lr/metrics.csv)):
- Average Precision = 0.2349
- Average Recall = 0.3146
- Average F1 = 0.2690

Comments: Data augmentation made all of the metrics worse by ~1%. Looking at the confusion matrices, the diagonal still looks clumpy, but less so than experiment 5. The predictions are more evenly spread across the classes instead of being concentrated on just 7 of them. So, the predictions are mostly still wrong, but at least there's less overfitting. The augmented data probably has something to do with this subjective improvement.

The data augmentation grew the training data 4x, but delivered no measurable improvement. If anything, it resulted in a nominal *decrease* in performance. Perhaps my assumptions about what makes a good data augmentation strategy are faulty, in which case the extra data would only introduce noise. I should validate if mirroring is acceptable or if I should replace and/or complement it with rotations...

The parameters for the sub-regions could still be non-optimal as well. See above.

On a technical note, the size of the data was causing the driver to run into memory/garbage collector problems, so I had to increase the memory allocated to the driver. The new command to run it is as follows: `spark-submit --class collinm.plankton.testing.LogisticRegressionRunner --driver-memory 4g --master local[7] build\libs\plankton-0-fat.jar output\experiment6.json doc\results\experiment6-lr 5`

FOLLOW-UP NOTE: adding `--conf 'spark.executor.memory=4g'` is also helpful in avoiding heap space problems when running on a machine that has enough room for large executor JVMs.

--------------------

**5/10/2016**

Setup Spark's random forest implementation in my process today; however, it will not run to completion. Using the trainng data from experiment 6, I am unable to even train a full forest. With any number of trees greater than 20, the job will throw an `OutOfMemoryError` citing java heap space and exit. With fewer trees than that, it will run for hours and show no progress. I tried again to run a job on the CMU Spark cluster, but it's still refusing my jobs. This bloated memory profile is apparently a known problem as there's a ticket to slim down the memory usage in the Spark issue tracker.

Since random forests weren't playing nice, I thought I might as well try out the next item in Spark's toolkit: multilayer perceptrons. I wrapped up this code and fed it the training data from experiment 6. It's running as I types this... should take ~3 hours (~40 mins per fold, 5 folds). Update: it took over 12 hours and the performance was terrible, see 5/11/2016.

So far, logistic regression has been the only thing that's tractable to iterate on quickly, but the model cannot accomodate non-linear decision boundaries. However, it's weakness here might be greatly compounded by the 121 boundaries that it has to cram into one model. I think it would be worth trying one random split of a one-vs-rest logistic regression model to see if posing the multi-class problem as 121 binary problems makes those features more usable. A quick literature search seems to support the hypothesis of OVR being better than multiclass when there are a lot of classes. Of course, it could be the case that the sub-region averages are just bad features, but that doesn't seem proven yet.

--------------------

**5/11/2016**

Created binning image histogram feature. The function of this processor is two fold: (1) it divides the space of pixel intensities into `n` bins depending on the specified bin size and uses those bins for its histogram, and (2) allows the user to set lower and upper cutoffs for pixel intensity values so that we can potentially ignore too high or too low values. The idea is to clean the signal aggregating together similar pixel values and excluding values that are not informative (i.e., white pixels = 255). This theory is partially informed by a [histogram of pixel values](imgs/pixel-intensity-hist-all.png) from all of the training data ([raw data](imgs/pixel-intensity-values.csv)). This specific histogram is limited at pixel intensity = 250 so that we can actually see the values lower than that. The amount of pixels with values >250 ranges from twice as many to two orders of magnitude greater. Surely this enormous concentration of pixels, which are effectively padding for the interesting part of the image, cannot be relevant? See the following experiments for results (spoiler: they are relevant).

**Experiment 6 - Multilayer Perceptron**  
Motivation/Hypothesis: Train a model capable of capturing non-linear relationships from the data.

Features: Same as experiment 6 above

Results of 5-fold multilayer perceptron ([Full results](results/experiment6-mlp/metrics.csv)):
- Average Precision = 0.0407
- Average Recall = 0.1432
- Average F1 = 0.0622

Comments: Really bad performance and took a long time to train. In hindsight, I just eyeballed the structure of the layers thinking that it would produce some kind of output of value, but that was not the case. The structure was [515, 500, 250, 121]. Epoch size was 256 which was perhaps too big...?

Looking at the confusion matrices, each model "chose" about 4-5 classes and would classify each record as one of those. The set of classes covered by all of the models was:
- acantharia_protist (2)
- chaetognath_non_sagitta (10)
- chaetognath_other (11)
- copepod_cyclopoid_oithona_eggs (24)
- diatom_chain_string (36)
- hydromedusae_shapeA (69)
- hydromedusae_solmaris (72)
- protist_other (85)
- trichodesmium_bowtie (108)
- trichodesmium_puff (110)

Scrolling through the images, I think that the histogram, shape, and size features contributed the most to the preference for these classes. Each one looks to have some combination of fairly distinct and consistent histogram, image shape, and image size. I think my sub-region averaging feature is a bust.

**Experiment 7 - Logistic Regression**: `collinm.plankton.training.Experiment7.java`  
Motivation/Hypothesis: will increasing the overlap parameter of the sub-region average feature increase the signal available to the model?

Note: I began running this experiment on the spark cluster before experiment 6 with the perceptron had finished.

Features:
- dimensions of image (height, width)
- pixel count
- normalize image size to 128x128
- histogram of pixel values (0-255)
- augment data by flipping image vertically (2x data)
- augment data by flipping image horizontally (2x data)
- sub-region averaging (8x8, 3 overlap)

Results of 5-fold logistic regression ([Full results](results/experiment7-lr/metrics.csv)):
- Average Precision = 0.2193
- Average Recall = 0.2878
- Average F1 = 0.2488

Comments: These results are best compared with experiment 6 from 5/9/2016. In every metric, these results are ~3% worse. This is not surprising given what I know now from the other experiments. The only thing left to try in regards to this feature would be a much smaller window size with no overlap, but that will create a ton of features.

**Experiment 4 - OVR Logistic Regression**  
Motivation/Hypothesis: Since my more advanced features have not been performing well, I'm returning to my most successful feature set and applying a different model, namely one vs rest logistic regression (instead of multi-class logistic regression).

Features:
- dimensions of image (height, width)
- pixel count
- normalize image size to 128x128
- histogram of pixel values (0-255)

Results of 5-fold OVR logistic regression ([Full results](results/experiment4-ovrlr/metrics.csv)):
- Average Precision = 0.2409
- Average Recall = 0.2786
- Average F1 = 0.2584

Comments: This experiment was stopped after only one fold as the performance was already far below many other experiments. This poor performance is a surprising result. I was expecting that the ensemble of 121 models would out-perform the single multi-class model by at least a little. My hypothesis as for why it hasn't is that many of the classes are very similar or somewhat derivative, e.g. classes that specify the original creature plus eggs or a specialized sort of antennae. In these cases, one of the models is consistently monopolizing the probability rank due to over-confidence or relative over-representation in the training data. Similarly, these very confident models are scooping up other classes that have very few examples in the data and giving higher probabilities to their classifications than is probably warranted. I may be able to correct this by only applying data augmentation techniques to the under-represented classes, maybe the bottom 50%?

**Experiment 8 - Logistic Regression**: `collinm.plankton.training.Experiment8.java`  
Motivation/Hypothesis: In the same theme of getting back to basics, I created another feature that bins the histogram and allows you to create a high and low cutoff for pixel values. The idea is that this should remove some noise from the histogram (enormous amounts of white pixels) *and* make the models faster to train. However, I am running the risk of removing signal in the process of binning the pixel values.

Features:
- dimensions of image (height, width)
- pixel count
- binned histogram of pixel values (bin size=5, range=0-250)

Results of 5-fold logistic regression ([Full results](results/experiment8-lr/metrics.csv)):
- Average Precision = 0.2731
- Average Recall = 0.3199
- Average F1 = 0.2946

Comments: Comparing directly against experiment 4 with logistic regression (prec=0.3169, rec=0.3590, f1=0.3366), these results are uniformly worse by ~4%. My hypothesis was that binning would make the signal cleaner by aggreagting similar pixel values, but instead that aggregation has removed important signal. I'd like to do another iteration of this experiment where the cutoffs are not used and only the binning is performed. This should help to elucidate the signal of all those white or very near white pixels.

**Experiment 8_1 - Logistic Regression**: `collinm.plankton.training.Experiment8_1.java`  
Motivation/Hypothesis: This experiment is conceptually similar to experiment 8 except that the pixel intensity cutoffs of the binning histogram will not be used. This should help to measure the signal from the white or very near white pixels.

Features:
- dimensions of image (height, width)
- pixel count
- binned histogram of pixel values (bin size=5, range=0-256)

Results of 5-fold logistic regression ([Full results](results/experiment8_1-lr/metrics.csv)):
- Average Precision = 0.2927
- Average Recall = 0.3364
- Average F1 = 0.3130

Comments: Results are ~2% better across all metrics, indiacting that the very high value pixels carry non-trivial signal. Do not throw those away! It also confirms that binning is throwing away signal, as these results are still ~2% worse than the normal histogram.

**Experiment 4 - Logistic Regression (v2)**: `collinm.plankton.training.Experiment4.java`  
Motivation/Hypothesis: I'm re-running this experiment in order to generate confusion matrices to compare against other experiments.

Features:
- dimensions of image (height, width)
- pixel count
- normalize image size to 128x128
- histogram of pixel values (0-255)

Results of 5-fold logistic regression ([Full results](results/experiment4-lr/metrics.csv)):
- Average Precision = 0.3169
- Average Recall = 0.3590
- Average F1 = 0.3366

Comments: As expected, results are exactly the same.

Questions:
- Running spark jobs with YARN seems to incur disk serialization at every step which massively slows down the job, how do we run jobs on pure spark using the CMU cluster?

--------------------

**5/12/2016**

**Experiment 4 - Random Forest (v1)**  
Motivation/Hypothesis: I couldn't get random forests to train in any reasonable amount of time with 500+ features or augmented datasets, so let's try to get it to run to completion on a smaller dataset.

Features:
- dimensions of image (height, width)
- pixel count
- normalize image size to 128x128
- histogram of pixel values (0-255)

Results of 5-fold random forest (100 trees, max depth 5) ([Full results](results/experiment4-rf-1/metrics.csv)):
- Average Precision = 0.1444
- Average Recall = 0.2290
- Average F1 = 0.1769

Comments: This didn't perform especially well, but it did actually *work* which is more than I can say for the other times I tried it. Knowing that this baseline configuration works, I will scale up from here. This job also completed much faster than I expected, it took less than an hour. I suspect that this is because I went a little crazy on the memory allotments. However, this makes me think that the memory was too low before. Perhaps it was enough memory to prevent heap space errors but too little memory to keep everything in memory and thus forcing spark to spill some of the data to disk. I don't really have a good way to test this though, other than keep allotting lots of memory for each job and see if the performance stays high.

**Experiment 5 - Random Forest (v1)**  
Motivation/Hypothesis: Since I know that random forests will work now and possibly complete in a reasonable amount of time, I'm going to try training many more trees with greater depth on a dataset that is highly likely to contain non-linear relationships, which random forests should be able to model better than logistic regression.

Features:
- dimensions of image (height, width)
- pixel count
- normalize image size to 128x128
- histogram of pixel values (0-255)
- sub-region averaging (8x8, 0 overlap)

Results of 5-fold random forest (500 trees, max depth 20) ([Full results](results/experiment5-rf-1/metrics.csv)):
- Average Precision = TODO
- Average Recall = TODO
- Average F1 = TODO

Comments: 
