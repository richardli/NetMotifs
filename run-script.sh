javaclib analysis/*.java data/*.java model/*.java util/*.java


#-------------------------------------------------------------------------------
# The configuration that seems to work best
#-------------------------------------------------------------------------------
# a0=2, b0=4, epsion=0.0001, c=1, d=1, a1=2, b1=2000, T=2000, seed=12345, M=120,
# dir=, start="6m", dict=dict1, delta=..., filename=t5e3, supervised=true, max=5000 
javalib -Xms10g model/PoissonModel2 2 4 0.0001 1 1 2 2000 2000 12345 120 /data/rwanda_anon/richardli/MotifwithNeighbour/ 6m 0707 dict1 DeltaNG testA1NG true 5000 20000 > ../../MotifwithNeighbour/log/testA1NG &
javalib -Xms10g model/PoissonModel2 2 4 0.0001 1 1 2 2000 2000 12345 120 /data/rwanda_anon/richardli/MotifwithNeighbour/ 6m 0707 dict1 Delta testA1 true 5000 20000 > ../../MotifwithNeighbour/log/testA1 &
javalib -Xms10g model/PoissonModel2 2 4 0.0001 1 1 2 2000 2000 12345 120 /data/rwanda_anon/richardli/MotifwithNeighbour/ 6m 0801 dict1 DeltaNG testA2NG true 5000 20000 > ../../MotifwithNeighbour/log/testA2NG &
javalib -Xms10g model/PoissonModel2 2 4 0.0001 1 1 2 2000 2000 12345 120 /data/rwanda_anon/richardli/MotifwithNeighbour/ 6m 0801 dict1 Delta testA2 true 5000 20000 > ../../MotifwithNeighbour/log/testA2 &



javalib -Xms10g model/PoissonModel2 2 4 0.0001 1 1 2 2000 20 12345 120 /data/rwanda_anon/richardli/MotifwithNeighbour/ 6m 0707 dict1 Delta test true 5000 20000 > ../../MotifwithNeighbour/log/test &

#-------------------------------------------------------------------------------
# The initial tests
#-------------------------------------------------------------------------------

# a0=2, b0=4, epsion=0.0001, c=1, d=1, a1=2, b1=2000, T=2000, seed=12345, M=120,
# dir=, start="6m", dict=..., delta=..., filename=t5e3, supervised=true, max=5000 
javalib -Xms10g model/PoissonModel2 2 4 0.0001 1 1 2 2000 2000 12345 120 /data/rwanda_anon/richardli/MotifwithNeighbour/ 6m 0707 dict0 DeltaNG test1NG true 5000 10000 > ../../MotifwithNeighbour/log/test1NG &


# a0=2, b0=4, epsion=0.0001, c=1, d=1, a1=2, b1=5000, T=2000, seed=12345, M=120,
# dir=, start="6m", dict=..., delta=..., filename=t5e3, supervised=true, max=5000 
javalib -Xms10g model/PoissonModel2 2 4 0.0001 1 1 2 5000 2000 12345 120 /data/rwanda_anon/richardli/MotifwithNeighbour/ 6m 0707 dict0 DeltaNG test2NG true 5000 10000 > ../../MotifwithNeighbour/log/test2NG &

# a0=2, b0=4, epsion=0.0001, c=1, d=1, a1=2, b1=5000, T=2000, seed=12345, M=120,
# dir=, start="6m", dict=..., delta=..., filename=t5e3, supervised=true, max=5000 
javalib -Xms10g model/PoissonModel2 2 4 0.0001 1 1 2 2000 2000 12345 120 /data/rwanda_anon/richardli/MotifwithNeighbour/ 6m 0707 dict1 DeltaNG test3NG true 5000 10000 > ../../MotifwithNeighbour/log/test3NG &


# a0=2, b0=4, epsion=0.0001, c=1, d=1, a1=2, b1=5000, T=4000, seed=12345, M=120,
# dir=, start="6m", dict=..., delta=..., filename=t5e3, supervised=true, max=5000 
javalib -Xms10g model/PoissonModel2 2 4 0.0001 1 1 2 5000 4000 12345 120 /data/rwanda_anon/richardli/MotifwithNeighbour/ 6m 0707 dict0 DeltaNG test4NG true 10000 10000 > ../../MotifwithNeighbour/log/test4NG &

# a0=2, b0=4, epsion=0.0001, c=1, d=1, a1=2, b1=5000, T=4000, seed=12345, M=120,
# dir=, start="6m", dict=..., delta=..., filename=t5e3, supervised=true, max=5000 
javalib -Xms10g model/PoissonModel2 2 4 0.0001 1 1 2 2000 4000 12345 120 /data/rwanda_anon/richardli/MotifwithNeighbour/ 6m 0707 dict1 DeltaNG test5NG true 10000 10000 > ../../MotifwithNeighbour/log/test5NG &


# a0=2, b0=4, epsion=0.0001, c=1, d=1, a1=2, b1=5000, T=4000, seed=12345, M=120,
# dir=, start="6m", dict=..., delta=..., filename=t5e3, supervised=true, max=5000 
javalib -Xms10g model/PoissonModel2 2 4 0.0001 1 1 2 5000 4000 12345 120 /data/rwanda_anon/richardli/MotifwithNeighbour/ 6m 0801 dict0 DeltaNG test6NG true 10000 10000 > ../../MotifwithNeighbour/log/test6NG &

# a0=2, b0=4, epsion=0.0001, c=1, d=1, a1=2, b1=5000, T=4000, seed=12345, M=120,
# dir=, start="6m", dict=..., delta=..., filename=t5e3, supervised=true, max=5000 
javalib -Xms10g model/PoissonModel2 2 4 0.0001 1 1 2 2000 4000 12345 120 /data/rwanda_anon/richardli/MotifwithNeighbour/ 6m 0801 dict1 DeltaNG test7NG true 10000 10000 > ../../MotifwithNeighbour/log/test7NG &
