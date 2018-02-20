package CSElabs;

import java.util.Arrays;

public class BankImpl {
    private int numberOfCustomers;	// the number of customers
    private int numberOfResources;	// the number of resources

    private int[] available; 	// the available amount of each resource
    private int[][] maximum; 	// the maximum demand of each customer
    private int[][] allocation;	// the amount currently allocated
    private int[][] need;		// the remaining needs of each customer

    /**
     * Constructor
     * @param resources - the initial available amount of each resource
     * @param numberOfCustomers - the number of customers
     */
    public BankImpl (int[] resources, int numberOfCustomers) {
        // set the number of resources
        this.numberOfResources = resources.length;

        // set the number of customers
        this.numberOfCustomers = numberOfCustomers;

        // set the value of bank resources to available
        this.available = Arrays.copyOf(resources, resources.length);

        // set the array size for maximum, allocation, and need
        this.maximum = new int[this.numberOfCustomers][this.numberOfResources];
        this.allocation = new int[this.numberOfCustomers][this.numberOfResources];
        this.need = new int[this.numberOfCustomers][this.numberOfResources];
    }


    /**
     * Get number of customers
     * @return numberCustomer - number of customers
     */
    public int getNumberOfCustomers() {
        return numberOfCustomers;
    }


    /**
     * Add a customer
     * @param customerNumber - the number of the customer
     * @param maximumDemand - the maximum demand for this customer
     */
    public void addCustomer(int customerNumber, int[] maximumDemand) {
        // initialize the maximum, allocation, need for this customer
        // check if the customer's maximum demand exceeds bank's available resource
        // *** compareArrays method down below ***

        // set value for maximum and need
        if (compareArrays(maximumDemand, available)) {
            maximum[customerNumber] = Arrays.copyOf(maximumDemand, maximumDemand.length);
            need[customerNumber] = Arrays.copyOf(maximumDemand, maximumDemand.length);
        } else  {
            System.out.println("Invalid request");
        }
    }


    /**
     * Output the value of available, maximum,
     * allocation, and need
     */
    public void getState() {
        System.out.println("\n***** CURRENT STATE ******");

        // print available
        System.out.println("Available Matrix: " + Arrays.toString(available));

        // print allocation
        System.out.println("Allocation Matrix: " + Arrays.deepToString(allocation));

        // print max
        System.out.println("Maximum Matrix: " + Arrays.deepToString(maximum));

        // print need
        System.out.println("Need Matrix: " + Arrays.deepToString(need) + "\n");
    }


    /**
     * Request resources
     * If the request is not granted, this method should print the error message
     * @param customerNumber - the customer requesting resources
     * @param request - the resources being requested
     * @return grant state - whether granting the request leaves the system in safe or unsafe state
     */
    public synchronized boolean requestResources(int customerNumber, int[] request) {
        // print the request
        System.out.println("Customer" + String.valueOf(customerNumber) + " requests: " + Arrays.toString(request));

        // check if request larger than need
        if (!compareArrays(request, need[customerNumber])) {
            System.out.println("INVALID: request larger than need");
            return false;
        }

        // check if request larger than available
        if (!compareArrays(request, available)) {
            System.out.println("INVALID: request larger than available");
            return false;
        }

        // check if the state is safe or not
        if (checkSafe(customerNumber, request)) {

            // if it is safe, allocate the resources to customer customerNumber
            for (int i = 0; i < request.length; i++) {
                available[i] -= request[i];
                allocation[customerNumber][i] += request[i];
                need[customerNumber][i] -= request[i];
            }
            return true;
        }

        // return state
        return false;
    }


    /**
     * Release resources
     * @param customerNumber - the customer releasing resources
     * @param release - the resources being released
     */
    public synchronized void releaseResources(int customerNumber, int[] release) {
        // print the release
        System.out.println(Arrays.toString(release));

        // release the resources from customer customerNumber
        for (int i = 0; i < release.length; i++) {
            available[i] += release[i];
            allocation[customerNumber][i] -= release[i];
            need[customerNumber][i] += release[i];
        }
    }



    private synchronized boolean checkSafe(int customerNumber, int[] request) {
        // check if the state is safe
        // initialize a finish vector
        boolean[] finish = new boolean[numberOfCustomers];
        for (int i = 0; i < finish.length; i++) {
            finish[i] = false;
        }

        // copy the available matrix to temp_available
        int[] temp_available = Arrays.copyOf(available, available.length);

        for (int i = 0; i < request.length; i++) {

            // subtract request from temp_available
            temp_available[i] -= request[i];

            // temporarily subtract request from need
            need[customerNumber][i] -= request[i];

            // temporarily add request to allocation
            allocation[customerNumber][i] += request[i];

            // if customer request exceed maximum, return false
            if (request[i] > maximum[customerNumber][i]) {
                return false;
            }
        }

        // check if the Bank's algorithm can finish based on safety algorithm
        int[] work = Arrays.copyOf(temp_available, temp_available.length);

        boolean possible = true;
        while (possible) {
            possible = false;
            for (int i = 0; i < finish.length; i++) {
                if (!finish[i] && compareArrays(need[i], work)) {
                    possible = true;
                    for (int j = 0; j < work.length; j++) {
                        work[j] += allocation[i][j];
                    }
                    finish[i] = true;
                }
            }
        }

        // restore the value of need and allocation for the customer
        for (int i = 0; i < request.length; i++) {
            need[customerNumber][i] += request[i];
            allocation[customerNumber][i] -= request[i];
        }

        // go through the finish to see if all value is true
        // return state
        for (int i = 0; i < finish.length; i++) {
            if (!finish[i]) {
                return false;
            }
        }
        return true;
    }


    /**
     * Check if any element in array1 exceeds the corresponding element in array2.
     * @param array1
     * @param array2
     * @return boolean 'true' if none of the elements in array1 exceeds the corresponding elements in array2.
     */
    private boolean compareArrays(int[] array1, int[] array2) {
        for (int i = 0; i < array1.length; i++) {
            if (array1[i] > array2[i]) {
                return false;
            }
        }
        return true;
    }
}