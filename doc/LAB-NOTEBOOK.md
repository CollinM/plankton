## Quick Links

- [Plankton Metadata](metadata.md)
- [Feature Research](feature-research.md)

--------------------

# Lab Notebook

**4/25/2015**

Investigated data: [data about data](metadata.md)

Many classes seem under-represented. Of the 121 classes, half of them have <115 records in that class. These are going to be more difficult to predict, unless they have very little variance between images (unlikely). And then there are 4 labels with over 1000 records. Should these be down-sampled? Probably not, already not very much data present.

[This histogram](doc/imgs/plankton_classes_counts.png) is a nice visualization of the size of each class and how that contributes to the overall training data. It makes it fairly clear that half of the data comes from classes with ~500 records or fewer. And about a fifth of it comes from classes with more than 1000 records.

[Image sizes](doc/imgs/image_sizes.png) are also much less consistent than I thought. Will need to look into how to handle that.

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
  - Mirror image
  - Invert color/greyscale
  - Zoom?
- Don't need to do image feature extraction in Spark! Too difficult to get everything to place nice. Just do it locally and use spark for loading the processed training data and building the models

--------------------

**5/5/2015**

