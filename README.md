# Plankton Classification

## Building the artifacts

Prerequisites:
- [Gradle](http://gradle.org/)

Gradle is my build tool of choice, therefore, it is employed here. Spark requires that any runtime libraries outside of the core framework be bundled with the input jar, so we need to use a "fat" jar. This jar is built with `gradle fatJar`. It can be distinguished from the other build artifacts placed in the `build/libs` directory by it's trailing identifier `-fat`.

## Running

### Training Jobs

Prerequisites:
- Compiled OpenCV binaries and Java wrapper
- Uncompressed training data in a sibling directory path: `data/train/<class-folders-here>/<images-here>`
- List of all classes, one per line, in a file: `data/all-classes.txt`

`collinm.plankton.training` contains java classes that each represent an experiment in transforming the image training data into feature vectors. Each class has a main that can be run without any arguments. The only gotcha is that the JVM must be run with the argument `-Djava.library.path=path/to/opencv`, e.g. `-Djava.library.path=lib/x64/`. This is required in order for the OpenCV java wrapper to link and run correctly, and the OpenCV java wrapper is required in order to interact with the images.

Every experiment expects a file containing a list of all the classes in the training data. This file is used to transform the string labels into numerical labels. For the sake of consistency, this file should be generated once and used for all experiments.

Each experiment file will generate a uniquely named (unique to that experiment) JSON output file and write it into the `output` directory. Each file contains a list of JSON objects listing the record's ID (string), label (int), feature count (int), and features (list of doubles). These files can be used with the "runners" in the following section in order to train and test models.

### Testing jobs

Prerequisites:
- "fat" jar (see "Building the artifacts" above)
- Vectorized training data

`collinm.plankton.testing` contains java classes (hereto referred to as "runners") that create and run spark jobs to train a model and evaluate its performance. Each runner wraps a specific model and contains a main that expects some arguments. See the documentation for each main for further detail. To launch these runners:
- Locally: `spark-submit --class <full-class-path> --master local[<num-cores>] plankton-<version>-fat.jar <args>`
- On a spark cluster: `spark-submit --class <full-class-path> --master spark plankton-<version>-fat.jar <args>`
- On a YARN cluster: `spark-submit --class <full-class-path> --master yarn plankton-<version>-fat.jar <args>`

Depending on the experimental feature vector that the runner is paired with, you may want to consider some of the following options:
- Large input data: `--driver-memory 4g` or larger
- Memory hungry algorithm (specifically, random forest): `--conf 'spark.executor.memory=4g'` or larger
