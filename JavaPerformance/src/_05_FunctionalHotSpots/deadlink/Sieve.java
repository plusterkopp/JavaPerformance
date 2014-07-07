package _05_FunctionalHotSpots.deadlink;

public final class Sieve {
    public static void calculate(int upperBound) {
        boolean[] elems = new boolean[upperBound];
    
        for (int i=0; i<upperBound; i++)
            elems[i] = true;
            
        for (int i=2; i<upperBound; ) {
            
            for (int j=2; i*j < upperBound; j++)
                elems[i*j] = false;
            
            int k=0;
            for (k=i+1; k<upperBound; k++)  
               if (elems[k])  {
                i=k;
                break;
               }
            if (k==upperBound)  break; 
        }
    }

}
