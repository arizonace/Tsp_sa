//********************************************************************
// tsp_sa.java
// A Java Applet for my Web Page that demonstrates the simulated annealing method
//  for estimating the answer to a Traveling Salesman Problem
// by Claude D. Edwards a.k.a. phoenix j. arizona
// September 22, 1996
//********************************************************************

import java.applet.*;
import java.awt.*;

//********************************************************************

public class Tsp_sa extends Applet implements Runnable
  {
    int width = 0;
    int height = 0;
    int length = 0;
    int iTempDelay = 50;
	int iChangeDelay = 10;

	int point_size = 12;
    String font_to_use = "TimesRoman";
	Font font = null;

	final String Title  = "Travelling Salesman Simulation";
	final String Author = "(c) 1996 Claude D. Edwards";

	//Panel panel;

	Color cityColor = Color.red;
	Color pathColor = Color.green;
	Color backColor = Color.black;
	Color authorColor = Color.blue;

	final int MAX_CITIES = 220;
	final int MIN_CITIES =  22;
    int iNumCities = 70;
    int aiCities [];
    int aiXCoord [];
    int aiYCoord [];
	int aiTranspCities[];

    double dPathLength   = 0.0;
	double dTemperature  = 0.5;
	double dCoolingRate  = 0.9;

	int iNumTempSteps = 100;

	Graphics g;


    //----------------------------------------------------------------

    public void init()
      {
        String parameter;

		g = getGraphics();

        parameter = getParameter("FONT");
        if (parameter != null)
          font_to_use = parameter;

        parameter = getParameter("POINT_SIZE");
        if (parameter != null)
          point_size = Integer.parseInt(parameter);

		font = new Font(font_to_use, Font.PLAIN, point_size);

        parameter = getParameter("TEMP_DELAY");
        if (parameter != null)
          iTempDelay = Integer.parseInt(parameter);

        parameter = getParameter("CHANGE_DELAY");
        if (parameter != null)
          iChangeDelay = Integer.parseInt(parameter);

        parameter = getParameter("NUM_CITIES");
        if (parameter != null)
          iNumCities = Integer.parseInt(parameter);

		if (iNumCities > MAX_CITIES)
			iNumCities = MAX_CITIES;
		if (iNumCities < MIN_CITIES)
			iNumCities = MIN_CITIES;


		aiCities = new int [iNumCities];
		aiXCoord = new int [iNumCities];
		aiYCoord = new int [iNumCities];

		aiTranspCities = new int [iNumCities]; // to avoid allocating it for every transpose


		// add the panel of buttons to the applet
		//panel = new Panel();
		//add ("Buttons", panel);
		//panel.add(new Button("Scramble"));
		//panel.add(new Button("Start"));

		// first get the width and height, then place the cities at random points
        width = size().width;
        height = size().height;
		for (int i = 0; i < iNumCities; ++i)
		{
			aiCities[i] = i;
			aiXCoord[i] = random_int (10, width  - 10);
			aiYCoord[i] = random_int (10, height - (10+point_size));
		}

		// calculate the starting path length
		calculate_pathlength();
      }

    //----------------------------------------------------------------
    public void start()
      {
        (new Thread(this)).start();
      }

    //----------------------------------------------------------------
    public void delay(int iDelay)
    {
        try
          {
            Thread.sleep(iDelay);
          }
        catch (InterruptedException e)
          {
          }
	}

    //----------------------------------------------------------------

    public void re_draw()
      {
        repaint();

        try
          {
            Thread.sleep(iTempDelay);
          }
        catch (InterruptedException e)
          {
          }
      }


    //----------------------------------------------------------------
    public void update(Graphics g)
    {
		// first blank the background to the desired background color
		g.setColor(backColor);
		g.fillRect(0, 0, width, height);

		// then call the paint function
		paint(g);
	}



    //----------------------------------------------------------------
    public void paint(Graphics g)
    {
		// blank the background to the desired background color
		g.setColor(backColor);
		g.fillRect(0, 0, width, height);

		// draw all the paths
		g.setColor(pathColor);
        for (int i = 0; i < iNumCities; i++)
		{
			int iNext = (i+1)%iNumCities;
			g.drawLine(aiXCoord[aiCities[i]], aiYCoord[aiCities[i]], aiXCoord[aiCities[iNext]], aiYCoord[aiCities[iNext]]);
		}

		// draw all the cities
		g.setColor(cityColor);
		for (int i = 0; i < iNumCities; i++)
			g.drawArc(aiXCoord[aiCities[i]]-3, aiYCoord[aiCities[i]]-3, 6,6, 0, 360);

		// draw the description and name
		g.setFont(font);
		int x;
		//x = (width - g.getFontMetrics().stringWidth(Title))/2;
		//g.drawString(Title, x, 2+point_size);
		x = (width - g.getFontMetrics().stringWidth(Author))/2;
		g.setColor(authorColor);
		g.drawString(Author, x, height - 2);

      }

	// formats and displays the text for the status bar
    //----------------------------------------------------------------
	public void show_status()
	{
		showStatus ("Path=" + dPathLength + "  Temperature=" + dTemperature); 
	}

	// generates a random integer 
    //----------------------------------------------------------------
	public int random_int(int iLowestPossible, int iHighestPossible)
	{
		int iRange = iHighestPossible - iLowestPossible + 1;
		double dRandom = Math.random() * (double)iRange;
		int iRandom = (int)dRandom + iLowestPossible;
		return iRandom;
	}

	// generates a random boolean
    //----------------------------------------------------------------
	public boolean random_boolean()
	{
		return (Math.random() > 0.5);
	}

	// calculate the distance between two points (pythagaros)
    //----------------------------------------------------------------
	public double alen(double x1, double y1, double x2, double y2)
	{
	   return  Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
	}

	// calculate the distance between two cities
    //----------------------------------------------------------------
	public double distance (int iCity1, int iCity2)
	{
		return alen(aiXCoord[iCity1], aiYCoord[iCity1], aiXCoord[iCity2], aiYCoord[iCity2]);
	}

	// calculates the total round trip path through the cities by totalling the distances
	// between consecutive cities
    //----------------------------------------------------------------
	public void calculate_pathlength()
	{
		dPathLength = 0.0;
		for (int i = 0; i < iNumCities; ++i)
		{
			int iNext = (i+1)%iNumCities;
			dPathLength += distance(aiCities[i], aiCities[iNext]);
		}
	}


	// this function determines whether or not to accept a particular change
	// to the order of the cities.  The change is always accepted if the change
	// is negative, i.e. the path gets shorter.  If the change is positive, the 
	// change may still be accepted.  The higher the temperature, the higher the
	// chance that a positive change will be accepted.  The greater the positive change
	// the lower the chance that it will be accepted.
    //----------------------------------------------------------------
	public boolean accept_change(double dChange)
	{
		if (dChange < 0.0)
			return true;

		// This formula is the heart of simulated annealing.  
		if ( Math.random() <  Math.exp(-dChange/dTemperature)) 
		{
			return true;
		}

		return false;
	}


	// figures out the change in total path length if a segment of cities
	// was transposed to another spot in the array
    //----------------------------------------------------------------
	public double transpose_cost(int aiMovCity[])
	{
	   aiMovCity[4] = (aiMovCity[3]+1)%iNumCities;				// the one after 3
	   aiMovCity[6] = (aiMovCity[2]+1)%iNumCities;				// the one after 2
	   aiMovCity[5] = (aiMovCity[1]+iNumCities-1)%iNumCities;	// the one before 1

	   return distance(aiCities[aiMovCity[1]], aiCities[aiMovCity[3]])
		   +  distance(aiCities[aiMovCity[2]], aiCities[aiMovCity[4]])
		   +  distance(aiCities[aiMovCity[5]], aiCities[aiMovCity[6]])
		   -  distance(aiCities[aiMovCity[2]], aiCities[aiMovCity[6]])
		   -  distance(aiCities[aiMovCity[1]], aiCities[aiMovCity[5]])
		   -  distance(aiCities[aiMovCity[3]], aiCities[aiMovCity[4]]);
	}

	// figures out the change in total path length if the order of a segment of cities
	// was reversed
    //----------------------------------------------------------------
	public double reverse_cost(int aiMovCity[])
	{
	   aiMovCity[4] = (aiMovCity[2]+1)%iNumCities;				// the one after 2
	   aiMovCity[3] = (aiMovCity[1]+iNumCities-1)%iNumCities;	// the one before 1

	   return distance(aiCities[aiMovCity[1]], aiCities[aiMovCity[4]])
		   +  distance(aiCities[aiMovCity[2]], aiCities[aiMovCity[3]])
		   -  distance(aiCities[aiMovCity[1]], aiCities[aiMovCity[3]])
		   -  distance(aiCities[aiMovCity[2]], aiCities[aiMovCity[4]]);
	}


	// transposes a segment of cities from one spot in the array to another
    //----------------------------------------------------------------
	public void transpose (int aiMovCity[])
	{
		int iStart;
		int iStop;
		int iTranspIndex = 0;


		// first draw the changes on the graph
		// -- 1) erase the old lines
		g.setColor(backColor);
		g.drawLine(aiXCoord[aiCities[aiMovCity[2]]], aiYCoord[aiCities[aiMovCity[2]]], 
				   aiXCoord[aiCities[aiMovCity[6]]], aiYCoord[aiCities[aiMovCity[6]]]);
		g.drawLine(aiXCoord[aiCities[aiMovCity[1]]], aiYCoord[aiCities[aiMovCity[1]]], 
				   aiXCoord[aiCities[aiMovCity[5]]], aiYCoord[aiCities[aiMovCity[5]]]);
		g.drawLine(aiXCoord[aiCities[aiMovCity[3]]], aiYCoord[aiCities[aiMovCity[3]]], 
				   aiXCoord[aiCities[aiMovCity[4]]], aiYCoord[aiCities[aiMovCity[4]]]);
		// -- 1) draw the new lines
		g.setColor(pathColor);
		g.drawLine(aiXCoord[aiCities[aiMovCity[1]]], aiYCoord[aiCities[aiMovCity[1]]], 
				   aiXCoord[aiCities[aiMovCity[3]]], aiYCoord[aiCities[aiMovCity[3]]]);
		g.drawLine(aiXCoord[aiCities[aiMovCity[2]]], aiYCoord[aiCities[aiMovCity[2]]], 
				   aiXCoord[aiCities[aiMovCity[4]]], aiYCoord[aiCities[aiMovCity[4]]]);
		g.drawLine(aiXCoord[aiCities[aiMovCity[5]]], aiYCoord[aiCities[aiMovCity[5]]], 
				   aiXCoord[aiCities[aiMovCity[6]]], aiYCoord[aiCities[aiMovCity[6]]]);

		// redraw the affected cities
		g.setColor(cityColor);
		for (int i = 1; i <= 6; i++)
			g.drawArc(aiXCoord[aiCities[aiMovCity[i]]]-3, aiYCoord[aiCities[aiMovCity[i]]]-3, 6,6, 0, 360);


		// copy the 3 contiguous segments that aiMovCity indexes
		// [1]..[2] + [4]..[5] + [6]..[3]
		// from aiCities into aiTranspCities,
		// then copy it back into aiCities

		// copy [1]..[2] inclusive
		iStart = aiMovCity[1];
		iStop  = aiMovCity[2];
		if (iStart > iStop)
			iStop += iNumCities; // in case of wrap-around
		for (int i = iStart; i <= iStop; ++i)
		{
			aiTranspCities[iTranspIndex++] = aiCities[i%iNumCities];
		}

		// copy [4]..[5] inclusive
		iStart = aiMovCity[4];
		iStop  = aiMovCity[5];
		if (iStart > iStop)
			iStop += iNumCities; // in case of wrap-around
		for (int i = iStart; i <= iStop; ++i)
		{
			aiTranspCities[iTranspIndex++] = aiCities[i%iNumCities];
		}

		// copy [6]..[3] inclusive
		iStart = aiMovCity[6];
		iStop  = aiMovCity[3];
		if (iStart > iStop)
			iStop += iNumCities; // in case of wrap-around
		for (int i = iStart; i <= iStop; ++i)
		{
			aiTranspCities[iTranspIndex++] = aiCities[i%iNumCities];
		}

		// copy the transposed array back into aiCities
		for (int i = 0; i < iNumCities; ++i)
			aiCities[i] = aiTranspCities[i];
	}


	// reverses a segment of cities in the city array
    //----------------------------------------------------------------
	public void reverse(int aiMovCity[], int iNumSelected)
	{
		int iNumSwaps = iNumSelected / 2;

		// first draw the changes on the graph
		// -- 1) erase the old lines
		g.setColor(backColor);
		g.drawLine(aiXCoord[aiCities[aiMovCity[1]]], aiYCoord[aiCities[aiMovCity[1]]], 
				   aiXCoord[aiCities[aiMovCity[3]]], aiYCoord[aiCities[aiMovCity[3]]]);
		g.drawLine(aiXCoord[aiCities[aiMovCity[2]]], aiYCoord[aiCities[aiMovCity[2]]], 
				   aiXCoord[aiCities[aiMovCity[4]]], aiYCoord[aiCities[aiMovCity[4]]]);
		// -- 1) draw the new lines
		g.setColor(pathColor);
		g.drawLine(aiXCoord[aiCities[aiMovCity[2]]], aiYCoord[aiCities[aiMovCity[2]]], 
				   aiXCoord[aiCities[aiMovCity[3]]], aiYCoord[aiCities[aiMovCity[3]]]);
		g.drawLine(aiXCoord[aiCities[aiMovCity[1]]], aiYCoord[aiCities[aiMovCity[1]]], 
				   aiXCoord[aiCities[aiMovCity[4]]], aiYCoord[aiCities[aiMovCity[4]]]);

		// redraw the affected cities
		g.setColor(cityColor);
		for (int i = 1; i <= 4; i++)
			g.drawArc(aiXCoord[aiCities[aiMovCity[i]]]-3, aiYCoord[aiCities[aiMovCity[i]]]-3, 6,6, 0, 360);

		int i1 = aiMovCity[1];
		int i2 = aiMovCity[2];
		for (int i = 0; i < iNumSwaps; ++i)
		{
			int iTemp = aiCities[i1];
			aiCities[i1] = aiCities[i2];
			aiCities[i2] = iTemp;

			// increase i1 and decrease i2 towards each other
			i1 = (i1 + 1)%iNumCities; 
			i2 = (i2 - 1 + iNumCities) %iNumCities;
		}
	}


    //----------------------------------------------------------------
    public void run()
    {
		int iMaxAcceptsPerTemp = 10 * iNumCities; // max number of changes accepted per temperature value
		int   aiMovCity[] = new int [7];

		// draw the graph in its correct initial state
		update(g);
		show_status();

		// loop for the number of temperature steps
		for (int iTempStep = 0; iTempStep < iNumTempSteps; ++ iTempStep)
		{
			// For each temperature step, try repeatedly to make changes
			// stop after trying 100 times the number of cities, or the limit
			// of accepted changes has been reached
			int iNumAccepts = 0;
			for (int iTry = 0; iTry < iNumCities * 100; ++iTry)
			{
				// pick the cities we are going to mess with
				aiMovCity[1] = random_int (0, iNumCities-1);
				aiMovCity[2] = random_int (0, iNumCities-2);
				// make sure the cities are distinct.
				if (aiMovCity[2] == aiMovCity[1])
					aiMovCity[2] = (aiMovCity[2] + 1) % iNumCities;
				// NOTE:  The selection goes from aiMovCity[1] thru aiMovCity[2] inclusive.
				//        It may therefore wrap around 0.
				// calculate the number of cities selected
				int iSelection = aiMovCity[2] - aiMovCity[1] + 1;
				if (iSelection < 1) // wraps around 0
				{
					iSelection = iNumCities - aiMovCity[1] + aiMovCity[2] + 1;
				}
				int iNotInSelection = iNumCities - iSelection;

				// for each try, do either a reversal or a transpose.
				if (random_boolean())	// do a transport
				{					
					// For a transpose we pick up the entire selection from [1] through [2] 
					// and put it between a third and fourth city. We therefore cannot do a transpose 
					// if there are too few cities outside the selection.
					if (iNotInSelection < 4)
						continue;
					aiMovCity[3] = (aiMovCity[2] + random_int (2, iNotInSelection-2)) % iNumCities;

					double dCost = transpose_cost(aiMovCity);
					if (accept_change(dCost))
					{
						++iNumAccepts;
						transpose(aiMovCity);
						dPathLength += dCost;
						show_status();
						delay(iChangeDelay);
					}

				}
				else					// do a reversal
				{
					// For a reversal, we pick up the entire selection from [1] through [2]
					// and flip it around.  So in the big picture, we get to [1] from the city
					// that used to follow [2] and from [2] we proceed to the city that used
					// to preceed [1]
					if (iNotInSelection < 2)
						continue;

					double dCost = reverse_cost(aiMovCity);
					if (accept_change(dCost))
					{
						++iNumAccepts;
						reverse(aiMovCity, iSelection);
						dPathLength += dCost;
						show_status();
						delay(iChangeDelay);
					}
				}

				// break early if we have made the maximum number of changes allowed
				// for a temperature step
				if (iNumAccepts >= iMaxAcceptsPerTemp)
					break;
			}

			dTemperature *= dCoolingRate;
			calculate_pathlength();
			re_draw();
			if (iNumAccepts == 0) // nothing was accepted for this temperature value
				break;
		}
		showStatus ("DONE:  Path=" + dPathLength + "  Temperature=" + dTemperature); 
    }

  }

