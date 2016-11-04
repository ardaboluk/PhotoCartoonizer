package source;

import java.util.Random;

public class QuickSelect {
	
	//Reference: http://en.wikipedia.org/wiki/Quickselect
	//and http://rosettacode.org/wiki/Quickselect_algorithm#Java
	
	public int select(int array[], int nthsmallestindex){
			
		int left = 0;
		int right = array.length - 1;
		Random rand = new Random();
		while (right > left) {
			int pivotIndex = rand.nextInt(right - left + 1) + left;
			pivotIndex = partition(array, left, right, pivotIndex);
			if (pivotIndex - left == nthsmallestindex) {
				right = left = pivotIndex;
			} else if (pivotIndex - left < nthsmallestindex) {
				nthsmallestindex -= pivotIndex - left + 1;
				left = pivotIndex + 1;
			} else {
				right = pivotIndex - 1;
			}
		}
		return array[left];
		
	}
	
	private int partition(int array[], int left, int right, int pivotIndex){
		
		int pivotValue = array[pivotIndex];
		swap(array, pivotIndex, right);
		int storeIndex = left;
		for(int i = left; i <= right - 1; i++){
			if(array[i] < pivotValue){
				swap(array, storeIndex, i);
				storeIndex++;
			}
		}
		swap(array, right, storeIndex);
		return storeIndex;
	}
	
	private void swap(int array[], int index1, int index2){
		
		if(index1 != index2){
			int temp_index1 = array[index1];
			array[index1] = array[index2];
			array[index2] = temp_index1;
		}
	}	

}
