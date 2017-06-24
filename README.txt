This README file is for Malware Family Classifier as a part of Research Project at University of Oxford
Submission date: 26/06/2017
Created by: Munir Geden

*************************************
Path definitions
*************************************
1. a “training data path” folder needs to be specified in config.properties where the Cuckoo report files should be placed
2. a “test data path” folder needs to be specified in config.properties where the Cuckoo report files should be placed
3. a "reports" folder needs to be specified in config.properties where the distinctive features and other output results of the classifications should be placed.

*****************************************************************************
How to configure the application?
*****************************************************************************
1. specify the “training” data path consist of report files by editing config.properties file or from the command line write: 
	-tp TRAINING_DATA_FOLDER_PATH
2. specify the “validation” data path consist of report files by editing config.properties file or from the command line write: 
	-vp TEST_DATA_FOLDER_PATH
3. specify the "reports" directory path for the application by editing config.properties file

You can play with the parameters from the configuration file(config.properties) based on your preferences. 

******************************************************************************
ATTENTION FOR JVM MEMORY SIZE!!!
******************************************************************************
Due to high memory consumption during analysis for some feature models do not forget to increase maximum memory size for JVM
	(ex:$java -Xms1G -Xmx6G -jar familyclassifier.jar ….)


******************************************************************************
How to extract distinctive features from training data?
******************************************************************************
1. to construct distinctive features: 
	-xf
	(ex:$java -Xms1G -Xmx6G -jar familyclassifier.jar -xf )


******************************************************************************
How to write feature matrices to weka files for training and test samples by using distinctive feature sets?
******************************************************************************
1. to generate weka arff files for training and test samples: 
	-gwf -df DISTINCTIVE_FEATURES_FILE_PATTERN
	(ex:$java -Xms1G -Xmx6G -jar familyclassifier.jar -gwf -df reports/json_distinctive_apicallwithargs_by_ )


******************************************************************************
How to classify wih Weka classifiers?
******************************************************************************
1. to classify Weka for the given algorithm with the given weka training and test *.arff files
   (knn:k-nearest neighbours(k value configurable from configuration.properties), 
    svm:support vector machines
    rf:random forest
    nn: neural networks) from the command line enter: 
	-cwa apicallwithargs
	(ex:$java -Xms1G -Xmx6G -jar familyclassifier.jar -cwa all -wt reports/train_class_ngram_4_1000.arff.arff -wv reports/test_class_ngram_4_1000.arff)
	this will generate output results in the reports folder

***************************************
Access to source code
***************************************
Link to project's repository:
https://bitbucket.org/msgeden/familyclassifier

For clonning the repository:
git clone https://bitbucket.org/msgeden/familyclassifier.git