package climate;

import java.util.ArrayList;

/**
 * This class contains methods which perform various operations on a layered 
 * linked list structure that contains USA communitie's Climate and Economic information.
 * 
 * @author Navya Sharma
 */

public class ClimateEconJustice {

    private StateNode firstState;
    
    /*
    * Constructor
    * 
    * **** DO NOT EDIT *****
    */
    public ClimateEconJustice() {
        firstState = null;
    }

    /*
    * Get method to retrieve instance variable firstState
    * 
    * @return firstState
    * 
    * **** DO NOT EDIT *****
    */ 
    public StateNode getFirstState () {
        // DO NOT EDIT THIS CODE
        return firstState;
    }

    /**
     * Creates 3-layered linked structure consisting of state, county, 
     * and community objects by reading in CSV file provided.
     * 
     * @param inputFile, the file read from the Driver to be used for
     * @return void
     * 
     * **** DO NOT EDIT *****
     */
    public void createLinkedStructure ( String inputFile ) {
        
        // DO NOT EDIT THIS CODE
        StdIn.setFile(inputFile);
        StdIn.readLine();
        
        // Reads the file one line at a time
        while ( StdIn.hasNextLine() ) {
            // Reads a single line from input file
            String line = StdIn.readLine();
            // IMPLEMENT these methods
            addToStateLevel(line);
            addToCountyLevel(line);
            addToCommunityLevel(line);
        }
    }

    /*
    * Adds a state to the first level of the linked structure.
    * Do nothing if the state is already present in the structure.
    * 
    * @param inputLine a line from the input file
    */
    public void addToStateLevel ( String inputLine ) {
    String[] data = inputLine.split(",");
    String stateName = data[2];
    StateNode state = new StateNode(stateName, null, null);
    if (firstState == null) {
        firstState = state;
    } else {
        StateNode current = firstState;
        while (current != null) {
            if (current.getName().equals(stateName)) {
                return; 
            }
            if (current.getNext() == null) { 
                current.setNext(state); 
                return; 
            }
            current = current.getNext();
        }
    }
}

        

    /*
    * Adds a county to a state's list of counties.
    * 
    * Access the state's list of counties' using the down pointer from the State class.
    * Do nothing if the county is already present in the structure.
    * 
    * @param inputFile a line from the input file
    */
    public void addToCountyLevel ( String inputLine ) {
        String[] data = inputLine.split(",");
        String stateName = data[2];
        String countyName = data[1];
        CountyNode county = new CountyNode(countyName, null, null);
        StateNode current = firstState;
        while (current != null) {
            if (current.getName().equals(stateName)) {
                CountyNode currentCounty = current.getDown();
                if (currentCounty == null) {
                    current.setDown(county);
                    return;
                } else {
                    while (currentCounty != null) {
                        if (currentCounty.getName().equals(countyName)) {
                            return;
                        }
                        if (currentCounty.getNext() == null) { 
                            currentCounty.setNext(county);
                            return;
                        }
                        currentCounty = currentCounty.getNext();
                    }
                }
            }
            current = current.getNext();
        }
    }

    /*
    * Adds a community to a county's list of communities.
    * 
    * Access the county through its state
    *      - search for the state first, 
    *      - then search for the county.
    * Use the state name and the county name from the inputLine to search.
    * 
    * Access the state's list of counties using the down pointer from the StateNode class.
    * Access the county's list of communities using the down pointer from the CountyNode class.
    * Do nothing if the community is already present in the structure.
    * 
    * @param inputFile a line from the input file
    */
    public void addToCommunityLevel(String inputLine) {
        String[] data = inputLine.split(",");
        String stateName = data[2];
        String countyName = data[1];
        String communityName = data[0];
        Data communityData = new Data(
            Double.parseDouble(data[3]),
            Double.parseDouble(data[4]),
            Double.parseDouble(data[5]),
            Double.parseDouble(data[8]),
            Double.parseDouble(data[9]),
            data[19], 
            Double.parseDouble(data[49]),
            Double.parseDouble(data[37]),
            Double.parseDouble(data[121])
        );

        CommunityNode community = new CommunityNode(communityName, null, communityData);

        StateNode current = firstState;
        while (current != null) {
            if (current.getName().equals(stateName)) {
                CountyNode currentCounty = current.getDown();
                while (currentCounty != null) {
                    if (currentCounty.getName().equals(countyName)) {
                        CommunityNode currentCommunity = currentCounty.getDown();
                        if (currentCommunity == null) {
                            currentCounty.setDown(community);
                            return;
                        } else {
                            while (currentCommunity != null) {
                                if (currentCommunity.getNext() == null) {
                                    currentCommunity.setNext(community);
                                    return;
                                }
                                currentCommunity = currentCommunity.getNext();
                            }
                        }
                    }
                    currentCounty = currentCounty.getNext();
                }
            }
            current = current.getNext();
        }
    }

    /**
     * Given a certain percentage and racial group inputted by user, returns
     * the number of communities that have that said percentage or more of racial group  
     * and are identified as disadvantaged
     * 
     * Percentages should be passed in as integers for this method.
     * 
     * @param userPrcntage the percentage which will be compared with the racial groups
     * @param race the race which will be returned
     * @return the amount of communities that contain the same or higher percentage of the given race
     */
    public int disadvantagedCommunities(double userPrcntage, String race) {
        int count = 0;
        StateNode currentState = firstState;
        while (currentState != null) {
            CountyNode currentCounty = currentState.getDown();
            while (currentCounty != null) {
                CommunityNode currentCommunity = currentCounty.getDown();
                while (currentCommunity != null) {
                    Data communityData = currentCommunity.getInfo();
                    if (communityData != null && communityData.getAdvantageStatus().equalsIgnoreCase("True")) {
                        double racePercentage = 0;
                        if (race.equalsIgnoreCase("african american")) {
                            racePercentage = communityData.getPrcntAfricanAmerican() * 100;
                        } else if (race.equalsIgnoreCase("native american")) {
                            racePercentage = communityData.getPrcntNative() * 100;
                        } else if (race.equalsIgnoreCase("asian american")) {
                            racePercentage = communityData.getPrcntAsian() * 100;
                        } else if (race.equalsIgnoreCase("white american")) {
                            racePercentage = communityData.getPrcntWhite() * 100;
                        } else if (race.equalsIgnoreCase("hispanic american")) {
                            racePercentage = communityData.getPrcntHispanic() * 100;
                        }
                        if (racePercentage >= userPrcntage) {
                            count++;
                        }
                    }
                    currentCommunity = currentCommunity.getNext();
                }
                currentCounty = currentCounty.getNext();
            }
            currentState = currentState.getNext();
        }
        return count;
    }
    /**
     * Given a certain percentage and racial group inputted by user, returns
     * the number of communities that have that said percentage or more of racial group  
     * and are identified as non disadvantaged
     * 
     * Percentages should be passed in as integers for this method.
     * 
     * @param userPrcntage the percentage which will be compared with the racial groups
     * @param race the race which will be returned
     * @return the amount of communities that contain the same or higher percentage of the given race
     */
    public int nonDisadvantagedCommunities(double userPrcntage, String race) {
        int count = 0;
        StateNode currentState = firstState;
        while (currentState != null) {
            CountyNode currentCounty = currentState.getDown();
            while (currentCounty != null) {
                CommunityNode currentCommunity = currentCounty.getDown();
                while (currentCommunity != null) {
                    Data communityData = currentCommunity.getInfo();
                    if (communityData != null && communityData.getAdvantageStatus().equalsIgnoreCase("False")) {
                        double racePercentage = 0;
                        if (race.equalsIgnoreCase("african american")) {
                            racePercentage = communityData.getPrcntAfricanAmerican() * 100;
                        } else if (race.equalsIgnoreCase("native american")) {
                            racePercentage = communityData.getPrcntNative() * 100;
                        } else if (race.equalsIgnoreCase("asian american")) {
                            racePercentage = communityData.getPrcntAsian() * 100;
                        } else if (race.equalsIgnoreCase("white american")) {
                            racePercentage = communityData.getPrcntWhite() * 100;
                        } else if (race.equalsIgnoreCase("hispanic american")) {
                            racePercentage = communityData.getPrcntHispanic() * 100;
                        }
                        if (racePercentage >= userPrcntage) {
                            count++;
                        }
                    }
                    currentCommunity = currentCommunity.getNext();
                }
                currentCounty = currentCounty.getNext();
            }
            currentState = currentState.getNext();
        }
        return count;
    }

    
    /** 
     * Returns a list of states that have a PM (particulate matter) level
     * equal to or higher than value inputted by user.
     * 
     * @param PMlevel the level of particulate matter
     * @return the States which have or exceed that level
     */ 
    public ArrayList<StateNode> statesPMLevels(double PMlevel) {
        ArrayList<StateNode> states = new ArrayList<>();
        StateNode currentState = firstState;
        while (currentState != null) {
            boolean stateAdded = false;
            CountyNode currentCounty = currentState.getDown();
            while (currentCounty != null && !stateAdded) {
                CommunityNode currentCommunity = currentCounty.getDown();
                while (currentCommunity != null && !stateAdded) {
                    Data communityData = currentCommunity.getInfo();
                    if (communityData.getPMlevel() >= PMlevel) {
                        states.add(currentState);
                        stateAdded = true;
                    }
                    currentCommunity = currentCommunity.getNext();
                }
                currentCounty = currentCounty.getNext();
            }
            currentState = currentState.getNext();
        }
        return states;
    }

    /**
     * Given a percentage inputted by user, returns the number of communities 
     * that have a chance equal to or higher than said percentage of
     * experiencing a flood in the next 30 years.
     * 
     * @param userPercntage the percentage of interest/comparison
     * @return the amount of communities at risk of flooding
     */
    public int chanceOfFlood ( double userPercentage ) {
        int count = 0;
        StateNode currentState = firstState;
        while (currentState != null) {
            CountyNode currentCounty = currentState.getDown();
            while (currentCounty != null) {
                CommunityNode currentCommunity = currentCounty.getDown();
                while (currentCommunity != null) {
                    Data communityData = currentCommunity.getInfo();
                    if (communityData.getChanceOfFlood() >= userPercentage) {
                        count++;
                    }
                    currentCommunity = currentCommunity.getNext();
                }
                currentCounty = currentCounty.getNext();
            }
            currentState = currentState.getNext();
        }

        return count;
    }

    /** 
     * Given a state inputted by user, returns the communities with 
     * the 10 lowest incomes within said state.
     * 
     *  @param stateName the State to be analyzed
     *  @return the top 10 lowest income communities in the State, with no particular order
    */
    public ArrayList<CommunityNode> lowestIncomeCommunities ( String stateName ) {

        ArrayList<CommunityNode> lowestIncomeCommunities = new ArrayList<>();
        StateNode currentState = firstState;
        while (currentState != null) {
            if (currentState.getName().equals(stateName)) {
                CountyNode currentCounty = currentState.getDown();
                while (currentCounty != null) {
                    CommunityNode currentCommunity = currentCounty.getDown();
                    while (currentCommunity != null) {
                        if (lowestIncomeCommunities.size() < 10) {
                            lowestIncomeCommunities.add(currentCommunity);
                        } else {
                            double lowestIncome = lowestIncomeCommunities.get(0).getInfo().getPercentPovertyLine();
                            int lowestIndex = 0;
                            for (int i = 1; i < lowestIncomeCommunities.size(); i++) {
                                if (lowestIncomeCommunities.get(i).getInfo().getPercentPovertyLine() < lowestIncome) {
                                    lowestIncome = lowestIncomeCommunities.get(i).getInfo().getPercentPovertyLine();
                                    lowestIndex = i;
                                }
                            }
                            if (currentCommunity.getInfo().getPercentPovertyLine() > lowestIncome) {
                                lowestIncomeCommunities.set(lowestIndex, currentCommunity);
                            }
                        }
                        currentCommunity = currentCommunity.getNext();
                    }
                    currentCounty = currentCounty.getNext();
                }
            }
            currentState = currentState.getNext();
        }
        return lowestIncomeCommunities;
    }
}
    
