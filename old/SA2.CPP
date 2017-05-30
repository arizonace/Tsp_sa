// sa2.cpp
// the traveling salesman problem
// using the simulated annealing method
// coded in c++ using graphics by phoenix k. arizona
// june 6, 1992


#include <conio.h>
#include <dos.h>
#include <graphics.h>
#include <math.h>
#include <stdlib.h>
#include <stdio.h>
#include <time.h>


#define NUMBER_OF_CITIES 220


// --======================== GLOBAL VARIABLES

   int NUM_CITIES;
   int CITIES [NUMBER_OF_CITIES+1];
   int XCOORD [NUMBER_OF_CITIES+1];
   int YCOORD [NUMBER_OF_CITIES+1];
   double PATH = 0.0, T= 0.0;

   unsigned long yes=0, no=0;


// --======================== FUNCTIONS

// ----------------------------------------------
inline double alen(long x1,  long x2, long y1, long y2)
   {
   /*
   double x1  = (double) X1;
   double x2  = (double) X2;
   double y1  = (double) Y1;
   double y2  = (double) Y2;
   */
   return sqrt ((double) ((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1)));
   }



// ----------------------------------------------
void draw_graph()
   {
//   clearviewport();
   setcolor(LIGHTRED);
   for (int i = 1; i <= NUM_CITIES; i++)
      {
      int jj = CITIES[i];
      if (i == 1)
	 {
	 moveto (XCOORD[jj], YCOORD[jj]);
	 }
      else
	 {
	 lineto ( XCOORD[jj], YCOORD[jj]);
	 }
      setcolor(LIGHTBLUE);
      circle (XCOORD[jj],YCOORD[jj],4);
      setcolor(LIGHTRED);
      }
   lineto (XCOORD[CITIES[1]], YCOORD[CITIES[1]]);
   gotoxy (20,1);
   printf("T= %7.7f, Path Length = %7.7f  ",T,PATH);
   return;
   }


// ----------------------------------------------
double revcst (int x[], int y[], int IORDER[], int NCITY, int N[7])
   {
   int XX[5], YY[5];

   N[3] = 1 + (N[1] + NCITY -2) % NCITY; //the city before N[1]
   N[4] = 1 + N[2] % NCITY;

   for (int j = 1; j <= 4; j++)
      {
      int i1 = IORDER[N[j]];
      XX[j] = x[i1];
      YY[j] = y[i1];
      }

   return alen(XX[1], XX[4], YY[1], YY[4]) +
	  alen(XX[2], XX[3], YY[2], YY[3]) -
	  alen(XX[1], XX[3], YY[1], YY[3]) -
	  alen(XX[2], XX[4], YY[2], YY[4]);
   }


// ----------------------------------------------
void revers( int IORDER[], int NCITY, int N[7])
   {

   setcolor(BLACK);
   setlinestyle(SOLID_LINE,0,1);
   line(XCOORD[CITIES[N[3]]], YCOORD[CITIES[N[3]]], XCOORD[CITIES[N[1]]], YCOORD[CITIES[N[1]]]);
   line(XCOORD[CITIES[N[4]]], YCOORD[CITIES[N[4]]], XCOORD[CITIES[N[2]]], YCOORD[CITIES[N[2]]]);
   setcolor(LIGHTRED);
   setlinestyle(SOLID_LINE,0,1);
   line(XCOORD[CITIES[N[3]]], YCOORD[CITIES[N[3]]], XCOORD[CITIES[N[2]]], YCOORD[CITIES[N[2]]]);
   line(XCOORD[CITIES[N[4]]], YCOORD[CITIES[N[4]]], XCOORD[CITIES[N[1]]], YCOORD[CITIES[N[1]]]);
   gotoxy(20,1);
   printf("T= %7.7f, Path Length = %7.7f ",T,PATH);

   int NN = (1 + (N[2] - N[1] + NCITY) % NCITY) /2;  // # of cities to swap
   for (int j = 1; j <= NN; j++)
      {
      int k= 1 + (N[1] + j - 2) % NCITY;
      int l= 1 + (N[2] - j + NCITY) % NCITY;

      int ITMP = IORDER[k];
      IORDER[k] = IORDER[l];
      IORDER[l] = ITMP;
      }
   return;
   }


// ----------------------------------------------
double trncst (int x[], int y[], int IORDER[], int NCITY, int N[7])
   {
   int XX[7], YY[7];

   N[4] = 1 + N[3] % NCITY; //the city following N[3]
   N[5] = 1 + (N[1] + NCITY -2) % NCITY; // the one before N[1]
   N[6] = 1 + N[2] % NCITY; // the one following N[2]

   for (int j = 1; j <= 6; j++)
      {
      int i1 = IORDER[N[j]];
      XX[j] = x[i1];
      YY[j] = y[i1];
      }

   return alen(XX[1], XX[3], YY[1], YY[3]) +
	  alen(XX[2], XX[4], YY[2], YY[4]) +
	  alen(XX[5], XX[6], YY[5], YY[6]) -
	  alen(XX[2], XX[6], YY[2], YY[6]) -
	  alen(XX[1], XX[5], YY[1], YY[5]) -
	  alen(XX[3], XX[4], YY[3], YY[4]);
   }


// ----------------------------------------------
void trnspt (int IORDER[], int NCITY, int N[7])
   {
   int JORDER [NUMBER_OF_CITIES+1];

   int M1 = 1 + (N[2] - N[1] + NCITY) % NCITY;
   int M2 = 1 + (N[5] - N[4] + NCITY) % NCITY;
   int M3 = 1 + (N[3] - N[6] + NCITY) % NCITY;
   int NN = 1;

   setcolor(BLACK);
   setlinestyle(SOLID_LINE,0,1);
   line(XCOORD[CITIES[N[3]]], YCOORD[CITIES[N[3]]], XCOORD[CITIES[N[4]]], YCOORD[CITIES[N[4]]]);
   line(XCOORD[CITIES[N[5]]], YCOORD[CITIES[N[5]]], XCOORD[CITIES[N[1]]], YCOORD[CITIES[N[1]]]);
   line(XCOORD[CITIES[N[2]]], YCOORD[CITIES[N[2]]], XCOORD[CITIES[N[6]]], YCOORD[CITIES[N[6]]]);
   setcolor(LIGHTRED);
   setlinestyle(SOLID_LINE,0,1);
   line(XCOORD[CITIES[N[5]]], YCOORD[CITIES[N[5]]], XCOORD[CITIES[N[6]]], YCOORD[CITIES[N[6]]]);
   line(XCOORD[CITIES[N[3]]], YCOORD[CITIES[N[3]]], XCOORD[CITIES[N[1]]], YCOORD[CITIES[N[1]]]);
   line(XCOORD[CITIES[N[2]]], YCOORD[CITIES[N[2]]], XCOORD[CITIES[N[4]]], YCOORD[CITIES[N[4]]]);
   gotoxy(20,1);
   printf("T= %7.7f, Path Length = %7.7f ",T,PATH);

   int j;  // loop variable

   for (j =1; j <= M1; j++)
      {
      int jj = 1 + (j+ N[1] - 2) % NCITY;
      JORDER[NN] = IORDER[jj];
      NN ++;
      }

   if ( M2 > 0)
      {
      for (j = 1; j <= M2; j++)
	 {
	 int jj = 1 + (j + N[4] -2 ) % NCITY;
	 JORDER[NN]=IORDER[jj];
	 NN++;
	 }
      }

   if ( M3 > 0)
      {
      for (j = 1; j <= M3; j++)
	 {
	 int jj = 1 + (j + N[6] - 2) % NCITY;
	 JORDER[NN] = IORDER[jj];
	 NN++;
	 }
      }

   for (j = 1; j <= NCITY; j++)
      {
      IORDER[j] = JORDER[j];
      }
   return;
   }


// ----------------------------------------------
int metrop (double DE, double T)
   {
   if ( DE < 0.0)
      {
      yes++;
      return 1;
      }

   int r = random(1000);

   double DoverT = -DE/T * 7/500;
   double ex = exp(DoverT);
   int e = (int) (1000.0 * ex);

   gotoxy(1,2);
   printf("%3d %4d %13.7lf %11.7lf %11.7lf  Yes:%6ld  No:%6ld  ",r,e,DE,DoverT,ex,yes,no);
/*
   if ( ((double) random(100)) < (100 * exp(-DE/T)))
      {
      return 1;
      }
*/

   if ( r < e)
      {
      yes++;
      return 1;
      }

   no++;
   return 0;
   }


// ----------------------------------------------
void scramble ( int INARRAY[], int max_el, int iterations)
   {
   int i, i1, i2, temp;

   for (i = 0; i < iterations; i++)
      {
      i1 = random(max_el) +1;
      i2 = random(max_el) +1;
      temp = INARRAY[i1];
      INARRAY[i1] = INARRAY[i2];
      INARRAY[i2] = temp;
      }
   }



/* this algo finds the shortest round-trip path to NCITY cities whose
   coordinates are in the arrays x[i], y[i].  The array IORDER specifies
   the order in which the arrays are visited.  On input, IORDER may be set
   to any permutation of the numbers 1 to NCITY.  This routine will return
   the best alternative path it can find
*/
// ----------------------------------------------
int anneal(int x[], int y[], int IORDER[], int NCITY)
   {
   int Ans;
   int    NOVER  = 100 * NCITY;
   int    NLIMIT = 10 * NCITY;
   int    N[7];
   double  TFACTR = 0.9;

   int i1, i2;   // temporary index variables
   // calculate initial path length
   PATH = 0.0;
   T = 7;
   for (int i = 1; i < NCITY; i++)
      {
      i1 = IORDER[i];
      i2 = IORDER[i+1];
      PATH += alen (x[i1], x[i2], y[i1], y[i2]);
      }
   // close the loop by tying path ends together
   i1 = IORDER[NCITY];
   i2 = IORDER[1];
   PATH += alen (x[i1], x[i2], y[i1], y[i2]);

   // try up to 100 temperature steps
   for (int j = 1; j <= 100; j++)
      {
      int NSUCC = 0;
      for (int k = 1; k<= NOVER; k++)
	 {
	 N[1] = 1 + random(NCITY);
	 N[2] = 1 + random(NCITY);
	 if (N[2] >= N[1])
	    {
	    N[2] %= NCITY;
	    N[2] += 1;
	    }
	 int NN = 1 +  (N[1] - N[2] + NCITY -1) % NCITY;
	 if (NN < 3) continue;

	 int IDEC = random(2);
	 if (IDEC == 0)  // do a transport
	    {
	    N[3] = 1 + N[2] + random(NN-2);
	    N[3] = 1 + (N[3]-1) % NCITY;
	    double DE = trncst(x,y, IORDER, NCITY, N);
	    Ans = metrop(DE, T);
	    if (Ans)
	       {
	       NSUCC ++;
	       PATH += DE;
	       trnspt(IORDER,NCITY,N);
	       }
	    }
	 else  // do a path reversal
	    {
	    double DE = revcst(x,y, IORDER, NCITY, N);
	    Ans = metrop(DE,T);
	    if (Ans)
	       {
	       NSUCC ++;
	       PATH += DE;
	       revers (IORDER,NCITY,N);
	       }
	    }
	 if (NSUCC >= NLIMIT) break;
	 if (kbhit() != 0)
	    {
	    switch (getch())
	       {
	       case  13:
	       case  32:
		    draw_graph();
		    break;
	       case 'Q':
	       case 'q':
	       case  27:
	       case   3:
		    return -1;
		    // break;
	       case 'P':
	       case 'p':
		    if (getch() == 0) getch();  //wait on a keypress
		    break;
	       case    0:
		    getch();
	       }
	    }
	 }
      T *= TFACTR;
      draw_graph();

      if (NSUCC == 0 ) return 0;
      }
   return 0;
   }



// --========================  MAIN  ===========================-- \\

void main (int argc, char *argv[])
   {
   int i;  // loop variable
   char title[82];


   if (argc > 1) NUM_CITIES = atoi(argv[1]);
   NUM_CITIES = (NUM_CITIES > NUMBER_OF_CITIES) ?
		 NUMBER_OF_CITIES : NUM_CITIES;
   NUM_CITIES = (NUM_CITIES == 0) ?
		 30 : NUM_CITIES;


   randomize();
   for (i = 1; i <= NUM_CITIES; i++)
      {
      CITIES[i] = i;
      XCOORD[i] = random(540) + 50;
      YCOORD[i] = random(400) + 50;
      }

   scramble(CITIES, NUM_CITIES, 2*NUM_CITIES);

   int gdriver = VGA, gmode = VGAHI;
   initgraph(&gdriver, &gmode, "");
   if ((graphresult()) != grOk)
      {
      printf("Ooops!  Graphics driver error!\n        ");
      printf("SA needs a VGA monitor, also, EGAVGA.BGI\n        ");
      printf("and SANS.CHR need to be in the current directory\n");
      return;
      }
   gotoxy (68,1);
   printf("[ESC] quits");
   sprintf(title,"Graphical TSP using SA by Claude D. Edwards    %d cities",
	    NUM_CITIES);
   settextstyle(SANS_SERIF_FONT,HORIZ_DIR,1);
   setcolor(LIGHTGREEN);
   outtextxy(5,450,title);
   setcolor(LIGHTRED);

   draw_graph();
   int res = anneal(XCOORD, YCOORD, CITIES, NUM_CITIES);
   draw_graph();
   gotoxy(1,1);
   if (res == 0)
      {
      printf ("done");
      }
   else
      {
      printf ("user terminate");
      }

   if (getch() == 0) getch();
   closegraph();
   gotoxy (1,10);
   printf ("simulated annealing problem coded by phoenix j. arizona\n");
   printf ("type SA <number of cities>, [ESC] quits, \
   [SPACE] refreshes screen\n");
   printf ("SA prints done in the upper left when it is finished\n");
   }