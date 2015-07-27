package model;
import cern.jet.random.tdouble.Gamma;
import cern.jet.random.tdouble.Normal;
import cern.jet.random.tdouble.engine.DoubleMersenneTwister;
import cern.jet.random.tdouble.engine.DoubleRandomEngine;

//import java.util.*;

/**
 * Class for testing simple Gibbs setup
 * @author zehang li
 * Last update: 07-27-15
 *
 */
class Gibbs
{
 
    public static void main(String[] arg)
    {
//    if (arg.length != 3) {
//            System.err.println("Usage: java Gibbs <Iters> <Thin> <Seed>");
//            System.exit(1);  
//        }
//    int N=Integer.parseInt(arg[0]);
//    int thin=Integer.parseInt(arg[1]);
//    int seed=Integer.parseInt(arg[2]);
    int N = 100;
    int thin = 10;
    int seed = 1; 
    DoubleRandomEngine rngEngine=new DoubleMersenneTwister(seed);
    Normal rngN=new Normal(0.0,1.0,rngEngine);
    Gamma rngG=new Gamma(1.0,1.0,rngEngine);
    double x=0,y=0;
    System.out.println("Iter x y");
    for (int i=0;i<N;i++) {
        for (int j=0;j<thin;j++) {
        x=rngG.nextDouble(3.0,y*y+4);
        y=rngN.nextDouble(1.0/(x+1),1.0/Math.sqrt(x+1));
        }
        System.out.println(i+" "+x+" "+y);
    }
    }
 
}