WSDM Cup 2017 Vandalism Detection Task: Feature Extraction
==========================================================

The [WSDM Cup 2017](https://www.wsdm-cup-2017.org/) was a data mining challenge held in conjunction with the 10th International Conference on Web Search and Data Mining (WSDM). The goal of the [vandalism detection task](https://www.wsdm-cup-2017.org/vandalism-detection.html) was to compute a vandalism score for each Wikidata revision denoting the likelihood of this revision being vandalism or similarly damaging. This is feature extraction component for the baselines WDVD, ORES, and FILTER. The classification and evaluation can be done with the corresponding [classification component](https://github.com/heindorf/wsdmcup17-wdvd-classification).

Paper
-----

This source code forms the basis for the overview paper of the [vandalism detection task at WSDM Cup 2017](https://arxiv.org/abs/1712.05956). When using the code, please make sure to refer to it as follows:

```TeX
@inproceedings{heindorf2017overview,
  author    = {Stefan Heindorf and
               Martin Potthast and
               Gregor Engels and
               Benno Stein},
  title     = {Overview of the Wikidata Vandalism Detection Task at {WSDM} Cup 2017},
  booktitle = {{{WSDM Cup 2017 Notebook Papers}},
  url       = {https://arxiv.org/abs/1712.05956},
  year      = {2017}
}
```

The code is based on the [Wikidata Vandalism Detector 2016](https://doi.acm.org/10.1145/2983323.2983740):

```TeX
@inproceedings{heindorf2016vandalism,
  author    = {Stefan Heindorf and
               Martin Potthast and
               Benno Stein and
               Gregor Engels},
  title     = {Vandalism Detection in Wikidata},
  booktitle = {{CIKM}},
  pages     = {327--336},
  publisher = {{ACM}},
  url       = {https://doi.acm.org/10.1145/2983323.2983740}
  year      = {2016}
}
```

Requirements
------------

The code depends on the [WSDM Cup 2017 Data Server](https://github.com/wsdm-cup-2017/wsdmcup17-data-server). It was tested with Java 8 Update 77, 64 Bit under Windows 10.

Installation
------------

In Eclipse, executing "Run As -> Maven install" creates a JAR file which includes all dependencies.

Execution
---------

The program can be executed with the script ´feature-extraction.sh´ which starts a data server for every file of the corpus. The paths to the jar files can be configured within the script.

Usage:

    ./feature-extraction.sh wsdmcup17_classification.py CORPUS FEATURES

Given a CORPUS directory, extract features and store them in the FEATURES file.

Example:

    ./feature-extraction.sh wdvc-2016/ features.csv.bz2

Required Data
-------------

- [Wikidata Vandalism Corpus 2016](https://www.wsdm-cup-2017.org/vandalism-detection.html#corpus-wdvc-16)

Computed Feature File
---------------------

The computed feature file is also available for download:

- [wsdmcup17_features.csv.bz2](https://groups.uni-paderborn.de/wdqa/wsdmcup17/wsdmcup17_features.csv.bz2)

Contact
-------

For questions and feedback please contact:

Stefan Heindorf, Paderborn University  
Martin Potthast, Leipzig University  
Gregor Engels, Paderborn University  
Benno Stein, Bauhaus-Universität Weimar

License
-------

The code by Stefan Heindorf, Martin Potthast, Gregor Engels, Benno Stein is licensed under a MIT license.
