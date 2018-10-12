package packagg;

public class ClassOne
{
   public ClassOne(final int x)
   {
      if (x > 0)
      {
         if (x % 1 == 0)
         {
            System.out.println("Even");
         }
         else
         {
            System.out.println("Odd");
         }
      }
      else
      {
         System.out.println("Not positive");
      }
   }
}
