# Running

## Training jobs

Prerequisites:
- Compiled OpenCV binaries and Java wrapper
- Uncompressed training data in a sibling directory path `data/train/<class-folders-here>/<images-here>`
- List of all classes, one per line, in a file `data/all-classes.txt`

Each of the java files in `collinm.plankton.training` contains a main class that can be run without any arguments passed to the class. The only gotcha is that the JVM must be run with the argument `-Djava.library.path=path/to/opencv`, e.g. `-Djava.library.path=lib/x64/`. This is required in order for the OpenCV java wrapper to link and run correctly.

Each experiment file will generate a uniquely named (unique to that experiment) JSON output file. These files are placed in the `output` directory and are quite simple. Each file contains a list of JSON objects listing the record's ID (string), label (int), and features (list of doubles). These files can be used with the "runners" in the following section in order to train and test models.

## Testing jobs

Each "runner" in `collinm.plankton.testing` contains a main with that runs a spark job. See the documentation for each main to see the arguments that it expects. These jobs are launched with commands similar to (depending on configuration):

`spark-submit --class <full-class-path> --master yarn plankton-<version>-fat.jar <args>`

Other options for specific cases:
- large input data: `--driver-memory 4g` or larger
- memory hungry algorithm: `--conf 'spark.executor.memory=4g'` or larger

# Building the artifacts

[Gradle](http://gradle.org/) is my build tool of choice, therefore, it is employed here. You can build the jar needed for running spark jobs with `grald fatJar`. This jar is special in that it not only contains the code that I built, but it also includes a select set of dependencies that are necessaryfor runtime but are not available within the cluster environment.
