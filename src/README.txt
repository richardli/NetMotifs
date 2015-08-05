# some running script for example

# build all
javac -cp ../library/\* analysis/*.java util/*.java data/*.java model/*.java

# unsupervised version
java -cp .:../library/\* -Xms20g model/SparseCoding_wrapper 10 1 1 1 1000 1000 1 500 98765 g1p1 g1p1 g1p1

# supervised version
java -cp .:../library/\* -Xms20g model/Supervised_wrapper 10 1 1 1 1000 1000 1 500 98765 5000 1 g3p1 g3p1 g3p1 