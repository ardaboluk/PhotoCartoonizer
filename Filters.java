package source;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Filters {
	
	//cartoonizes image
	public BufferedImage cartoonizeImage(BufferedImage image){
		
		int segmentationClusterCount = 32;
		int segmentationLoopCount = 10;
		int medianFilterWindowWidth = 9;
		int medianFilterWindowHeight = 9;
		int edgeDetectionThreshold = 100;
		
		BufferedImage segmentedImage = null;
		BufferedImage medianFilteredImage = null;
		BufferedImage edgeDetectedImage = null;
		
		segmentedImage = applyImageSegmentation(image, segmentationClusterCount, segmentationLoopCount);
		medianFilteredImage = applyMedianFilter(segmentedImage, medianFilterWindowWidth, medianFilterWindowHeight);				
		edgeDetectedImage = applyEdgeDetection(medianFilteredImage, edgeDetectionThreshold);		
		
		return edgeDetectedImage;
	}
	
	//applies median filter
	private BufferedImage applyMedianFilter(BufferedImage image, int windowWidth, int windowHeight){
		
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();
		int windowMiddle = windowWidth * windowHeight / 2;
		
		BufferedImage resultImage = new BufferedImage(imageWidth, imageHeight, image.getType());
		
		QuickSelect quickSelect = new QuickSelect();
		
		//median filter algorithm. Reference: http://en.wikipedia.org/wiki/Median_filter
		
		int edgex = windowWidth / 2;
		int edgey = windowHeight / 2;		
		for(int counterx = edgex; counterx < imageWidth - edgex; counterx++){
			for(int countery = edgey; countery < imageHeight - edgey; countery++){
				
				int colorArrayRed[] = new int[windowWidth * windowHeight];
				int colorArrayGreen[] = new int[windowWidth * windowHeight];
				int colorArrayBlue[] = new int[windowWidth * windowHeight];
				for(int windowx = 0; windowx < windowWidth; windowx++){
					for(int windowy = 0; windowy < windowHeight; windowy++){
						Color currentPixel = new Color(image.getRGB(counterx + windowx - edgex, countery + windowy - edgey));
						colorArrayRed[windowy * windowWidth + windowx] = currentPixel.getRed();
						colorArrayGreen[windowy * windowWidth + windowx] = currentPixel.getGreen();
						colorArrayBlue[windowy * windowWidth + windowx] = currentPixel.getBlue();
					}				
				}
				
				int medianRed = quickSelect.select(colorArrayRed, windowMiddle);
				int medianGreen = quickSelect.select(colorArrayGreen, windowMiddle);
				int medianBlue = quickSelect.select(colorArrayBlue, windowMiddle);
				int medianARGB = new Color(medianRed, medianGreen, medianBlue, 255).getRGB();
				
				resultImage.setRGB(counterx, countery, medianARGB);
			}
		}
		
		return resultImage;
	}

	// applies edge detection
	private BufferedImage applyEdgeDetection(BufferedImage image, int threshold) {

		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();

		// result image that will be returned
		BufferedImage resultImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());

		// it's unnecessary to apply k means clustering to color image
		int greyscaleimage[] = convertImageToGreyscale(image);

		// use Sobel kernel edge detecion technique:
		// http://dasl.mem.drexel.edu/alumni/bGreen/www.pages.drexel.edu/_weg22/edge.html
		
		int SUM = 0;

		// 3x3 GX Sobel mask
		int GX[][] = new int[3][3];
		// 3x3 GY Sobel mask
		int GY[][] = new int[3][3];

		GX[0][0] = -1; GX[0][1] = 0; GX[0][2] = 1;
		GX[1][0] = -2; GX[1][1] = 0; GX[1][2] = 2;
		GX[2][0] = -1; GX[2][1] = 0; GX[2][2] = 1;

		GY[0][0] = 1; GY[0][1] = 2; GY[0][2] = 1;
		GY[1][0] = 0; GY[1][1] = 0; GY[1][2] = 0;
		GY[2][0] = -1; GY[2][1] = -2; GY[2][2] = -1;

		for (int countery = 0; countery < imageHeight; countery++) {
			for (int counterx = 0; counterx < imageWidth; counterx++) {

				int sumX = 0;
				int sumY = 0;

				// image boundries
				if (countery == 0 || countery == imageHeight - 1) {
					SUM = 0;
				} else if (counterx == 0 || counterx == imageWidth - 1) {
					SUM = 0;
				} else {

					// X gardient approximation
					for (int i = -1; i <= 1; i++) {
						for (int j = -1; j <= 1; j++) {
							sumX += greyscaleimage[counterx + i + (countery + j) * imageWidth] * GX[i + 1][j + 1];
						}
					}

					// Y gradient approximation
					for (int i = -1; i <= 1; i++) {
						for (int j = -1; j <= 1; j++) {
							sumY += greyscaleimage[counterx + i + (countery + j) * imageWidth] * GY[i + 1][j + 1];
						}
					}

					// gradient magnitude
					SUM = (int) Math.sqrt(Math.pow(sumX, 2) + Math.pow(sumY, 2));

				}

				// if current pixel is an edge set this pixel to black,
				// otherwise set this pixel to current pixel of the original
				// image
				if (SUM > threshold) {
					resultImage.setRGB(counterx, countery, new Color(0, 0, 0, 255).getRGB());
				} else {
					resultImage.setRGB(counterx, countery, image.getRGB(counterx, countery));
				}

			}
		}

		return resultImage;
	}
	
	//this method applies image segmentation using k-means clustering algorithm
	BufferedImage applyImageSegmentation(BufferedImage image, int k, int loopLimit){
		
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();
		
		//result image that will be returned
		BufferedImage resultImage = new BufferedImage(imageWidth, imageHeight, image.getType());
		
		//color components of cluster centers
		int clusterCentersRed[] = new int[k];
		int clusterCentersGreen[] = new int[k];
		int clusterCentersBlue[] = new int[k];
		
		//clusters holding coordinates of image pixels
		ArrayList<ArrayList<Point>> clusters = new ArrayList<ArrayList<Point>>(k);
		for(int i = 0; i < k; i++){
			clusters.add(new ArrayList<Point>());
		}
		
		//k-means clustering algorithm. Reference: http://en.wikipedia.org/wiki/Image_segmentation#Clustering_methods
		
		//initialize cluster centers randomly
		for(int i = 0; i < k; i++){
			clusterCentersRed[i] = (int)(Math.random() * 256);
			clusterCentersGreen[i] = (int)(Math.random() * 256);
			clusterCentersBlue[i] = (int)(Math.random() * 256);
		}
		
		//populate the clusters at the beginning so that following iterations will get elements from clusters instead of the image
		for(int i = 0; i < imageHeight; i++){
			for(int j = 0; j < imageWidth; j++){
				
				int currentClusterCenterIndex = 0;
				
				Color currentPixel = new Color(image.getRGB(j, i));
				
				int currentPixelRed = currentPixel.getRed();
				int currentPixelGreen = currentPixel.getGreen();
				int currentPixelBlue = currentPixel.getBlue();
				
				int currentClusterCenterRed = clusterCentersRed[currentClusterCenterIndex];
				int currentClusterCenterGreen = clusterCentersGreen[currentClusterCenterIndex];
				int currentClusterCenterBlue = clusterCentersBlue[currentClusterCenterIndex];
				
				//index of the cluster center that has the smallest Euclidean distance to the current pixel
				int smallestEuclideanDistanceClusterCenterIndex = currentClusterCenterIndex;
				//holds smallest Euclidean distance between currentPixel and the cluster centers
				double smallestEuclideanDistance = Math.sqrt(Math.pow(currentPixelRed - currentClusterCenterRed, 2) + Math.pow(currentPixelGreen - currentClusterCenterGreen, 2)
						+ Math.pow(currentPixelBlue - currentClusterCenterBlue, 2));
				
				//find the cluster center that has the smallest distance to the current pixel
				currentClusterCenterIndex++;				
				for(; currentClusterCenterIndex < k; currentClusterCenterIndex++){					
					
					currentClusterCenterRed = clusterCentersRed[currentClusterCenterIndex];
					currentClusterCenterGreen = clusterCentersGreen[currentClusterCenterIndex];
					currentClusterCenterBlue = clusterCentersBlue[currentClusterCenterIndex];					
					
					double currentEuclideanDistance = Math.sqrt(Math.pow(currentPixelRed - currentClusterCenterRed, 2) + Math.pow(currentPixelGreen - currentClusterCenterGreen, 2)
							+ Math.pow(currentPixelBlue - currentClusterCenterBlue, 2));
					
					if(currentEuclideanDistance < smallestEuclideanDistance){
						smallestEuclideanDistance = currentEuclideanDistance;
						smallestEuclideanDistanceClusterCenterIndex = currentClusterCenterIndex;
					}
				}
				
				//assign current pixel to the cluster whose center has the smallest distance to the pixel
				clusters.get(smallestEuclideanDistanceClusterCenterIndex).add(new Point(j, i));
			}		
		}
		
		//compute cluster centers
		for(int i = 0; i < k; i++){
			
			int sumOfReds = 0;
			int sumOfGreens = 0;
			int sumOfBlues = 0;
			
			ArrayList<Point> currentCluster = clusters.get(i);
			for(int j = 0; j < currentCluster.size(); j++){
				
				Point currentPixelCoords = currentCluster.get(j);
				Color currentColor = new Color(image.getRGB(currentPixelCoords.x, currentPixelCoords.y));
				
				sumOfReds += currentColor.getRed();
				sumOfGreens += currentColor.getGreen();
				sumOfBlues += currentColor.getBlue();
			}
			
			if(currentCluster.size() != 0){
				clusterCentersRed[i] = sumOfReds / currentCluster.size();
				clusterCentersGreen[i] = sumOfGreens / currentCluster.size();
				clusterCentersBlue[i] = sumOfBlues / currentCluster.size();
			}
		}
		
		//repeat until no element changed its cluster
		int loopCount = 0;
		while(loopCount < loopLimit){
			
			//reset elementChanged so that we can understand if any element changed its cluster
			boolean elementChanged = false;
			
			//assign pixels to clusters using Euclidean distance
			for(int i = 0; i < k; i++){				
				
				ArrayList<Point> currentCluster = clusters.get(i);				 
				for(int j = 0; j < currentCluster.size(); j++){
					
					int tempClusterCenterIndex = 0;
					
					Color currentPixel = new Color(image.getRGB(currentCluster.get(j).x, currentCluster.get(j).y));				
					int currentPixelRed = currentPixel.getRed();
					int currentPixelGreen = currentPixel.getGreen();
					int currentPixelBlue = currentPixel.getBlue();
					
					int tempClusterCenterRed = clusterCentersRed[tempClusterCenterIndex];
					int tempClusterCenterGreen = clusterCentersGreen[tempClusterCenterIndex];
					int tempClusterCenterBlue = clusterCentersBlue[tempClusterCenterIndex];
					
					//index of the cluster center that has the smallest Euclidean distance to the current pixel
					int smallestEuclideanDistanceClusterCenterIndex = tempClusterCenterIndex;
					//holds smallest Euclidean distance between currentPixel and the cluster centers
					double smallestEuclideanDistance = Math.sqrt(Math.pow(currentPixelRed - tempClusterCenterRed, 2) + Math.pow(currentPixelGreen - tempClusterCenterGreen, 2)
							+ Math.pow(currentPixelBlue - tempClusterCenterBlue, 2));
					
					//find the cluster center that has the smallest distance to the current pixel
					tempClusterCenterIndex++;				
					for(; tempClusterCenterIndex < k; tempClusterCenterIndex++){					
						
						tempClusterCenterRed = clusterCentersRed[tempClusterCenterIndex];
						tempClusterCenterGreen = clusterCentersGreen[tempClusterCenterIndex];
						tempClusterCenterBlue = clusterCentersBlue[tempClusterCenterIndex];					
						
						double currentEuclideanDistance = Math.sqrt(Math.pow(currentPixelRed - tempClusterCenterRed, 2) + Math.pow(currentPixelGreen - tempClusterCenterGreen, 2)
								+ Math.pow(currentPixelBlue - tempClusterCenterBlue, 2));
						
						if(currentEuclideanDistance < smallestEuclideanDistance){
							smallestEuclideanDistance = currentEuclideanDistance;
							smallestEuclideanDistanceClusterCenterIndex = tempClusterCenterIndex;
						}
					}
					
					//assign current pixel to the cluster whose center has the smallest distance to the pixel
					clusters.get(smallestEuclideanDistanceClusterCenterIndex).add(currentCluster.get(j));
					//remove current pixel from the current cluster
					currentCluster.remove(j);
					
					//make elementChanged true if currentPixel changed cluster, that is
					//i is different from smallesEuclideanDistanceClusterCenterIndex
					if(i != smallestEuclideanDistanceClusterCenterIndex){
						elementChanged = true;
					}
					 
				}
			}
			
			//break loop if no element changed its cluster
			if(elementChanged == false){
				break;
			}
			
			//re-compute cluster centers
			for(int i = 0; i < k; i++){
				
				int sumOfReds = 0;
				int sumOfGreens = 0;
				int sumOfBlues = 0;
				
				ArrayList<Point> currentCluster = clusters.get(i);
				for(int j = 0; j < currentCluster.size(); j++){
					
					Point currentPixelCoords = currentCluster.get(j);
					Color currentColor = new Color(image.getRGB(currentPixelCoords.x, currentPixelCoords.y));
					sumOfReds += currentColor.getRed();
					sumOfGreens += currentColor.getGreen();
					sumOfBlues += currentColor.getBlue();
				}
				
				if(currentCluster.size() != 0){
					clusterCentersRed[i] = sumOfReds / currentCluster.size();
					clusterCentersGreen[i] = sumOfGreens / currentCluster.size();
					clusterCentersBlue[i] = sumOfBlues / currentCluster.size();
				}
			}
			
			loopCount++;
			
		}
		
		//construct the result image with respect to the clusters
		for(int i = 0; i < k; i++){
			
			ArrayList<Point> currentCluster = clusters.get(i);
			Color currentClusterCenterColor = new Color(clusterCentersRed[i], clusterCentersGreen[i], clusterCentersBlue[i]);
			for(int j = 0; j < currentCluster.size(); j++){				
				
				resultImage.setRGB(currentCluster.get(j).x, currentCluster.get(j).y, currentClusterCenterColor.getRGB());
			}
		}
		
		
		return resultImage;
	}

	// this method is used by applyEdgeDetection and applyImageSegmentation methods to get greyscale of the
	// given image
	private int[] convertImageToGreyscale(BufferedImage image) {

		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();
		
		int greyscale[] = new int[imageHeight * imageWidth];

		// Use luminosity method. Reference:
		// http://www.johndcook.com/blog/2009/08/24/algorithms-convert-color-grayscale/

		for (int countery = 0; countery < imageHeight; countery++) {
			for (int counterx = 0; counterx < imageWidth; counterx++) {

				// get current pixel
				Color currentPixel = new Color(image.getRGB(counterx, countery));
				int currentPixelRed = currentPixel.getRed();
				int currentPixelGreen = currentPixel.getGreen();
				int currentPixelBlue = currentPixel.getBlue();

				greyscale[imageWidth * countery + counterx] = (int) (0.21 * currentPixelRed + 0.71 * currentPixelGreen + 0.07 * currentPixelBlue);
			}
		}

		return greyscale;
	}

}
