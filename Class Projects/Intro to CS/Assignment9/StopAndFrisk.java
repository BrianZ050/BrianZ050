import java.util.ArrayList;

/**
 * The StopAndFrisk class represents stop-and-frisk data, provided by
 * the New York Police Department (NYPD), that is used to compare
 * during when the policy was put in place and after the policy ended.
 * 
 * @author Tanvi Yamarthy
 * @author Vidushi Jindal
 */
public class StopAndFrisk {

    /*
     * The ArrayList keeps track of years that are loaded from CSV data file.
     * Each SFYear corresponds to 1 year of SFRecords. 
     * Each SFRecord corresponds to one stop and frisk occurrence.
     */ 
    private ArrayList<SFYear> database; 

    /*
     * Constructor creates and initializes the @database array
     * 
     * DO NOT update nor remove this constructor
     */
    public StopAndFrisk () {
        database = new ArrayList<>();
    }

    /*
     * Getter method for the database.
     * *** DO NOT REMOVE nor update this method ****
     */
    public ArrayList<SFYear> getDatabase() {
        return database;
    }

    /**
     * This method reads the records information from an input csv file and populates 
     * the database.
     * 
     * Each stop and frisk record is a line in the input csv file.
     * 
     * 1. Open file utilizing StdIn.setFile(csvFile)
     * 2. While the input still contains lines:
     *    - Read a record line (see assignment description on how to do this)
     *    - Create an object of type SFRecord containing the record information
     *    - If the record's year has already is present in the database:
     *        - Add the SFRecord to the year's records
     *    - If the record's year is not present in the database:
     *        - Create a new SFYear 
     *        - Add the SFRecord to the new SFYear
     *        - Add the new SFYear to the database ArrayList
     * 
     * @param csvFile
     */
    public void readFile ( String csvFile ) {

        // DO NOT remove these two lines
        StdIn.setFile(csvFile); // Opens the file
        StdIn.readLine();       // Reads and discards the header line

        while(StdIn.hasNextLine()){
            String[] recordEntries = StdIn.readLine().split(","); 
            int year = Integer.parseInt(recordEntries[0]);
            String description = recordEntries[2];
            String gender = recordEntries[52];
            String race = recordEntries[66];
            String location = recordEntries[71];
            Boolean arrested = recordEntries[13].equals("Y");
            Boolean frisked = recordEntries[16].equals("Y");
            SFRecord record = new SFRecord (description, arrested, frisked, gender, race, location);

            int check=0;
        for (int i = 0; i<database.size(); i++){
            SFYear sfYear = database.get(i);
            if(sfYear.getcurrentYear() == year){
                sfYear.addRecord(record);
                check=1;
                break;
            }
        }
        if (check==0){
            SFYear newYear = new SFYear(year);
            newYear.addRecord(record);
            database.add(newYear);
        }
    }
}


    /**
     * This method returns the stop and frisk records of a given year where 
     * the people that was stopped was of the specified race.
     * 
     * @param year we are only interested in the records of year.
     * @param race we are only interested in the records of stops of people of race. 
     * @return an ArrayList containing all stop and frisk records for people of the 
     * parameters race and year.
     */

    public ArrayList<SFRecord> populationStopped ( int year, String race ) {
        ArrayList<SFRecord> recordsByRace = new ArrayList<SFRecord>();
        for(int i=0; i<database.size();i++){
            if(database.get(i).getcurrentYear() == year){
                ArrayList<SFRecord> records=database.get(i).getRecordsForYear();
                for (int j=0; j<records.size();j++){
                    if(records.get(j).getRace().equals(race)){
                        recordsByRace.add(records.get(j));
                    }
                }
            }
        }
        return recordsByRace;
    }

    /**
     * This method computes the percentage of records where the person was frisked and the
     * percentage of records where the person was arrested.
     * 
     * @param year we are only interested in the records of year.
     * @return the percent of the population that were frisked and the percent that
     *         were arrested.
     */
    public double[] friskedVSArrested ( int year ) {
        
        int populationFrisked=0;
        int populationArrested=0;
        double[] result=new double[2];
        for (int i=0; i<database.size();i++){
            if (database.get(i).getcurrentYear() == year){
                ArrayList<SFRecord> files = database.get(i).getRecordsForYear();
                for(int j=0; j<files.size(); j++){
                    if(files.get(j).getFrisked()==true){
                        populationFrisked++;
                    }
                    if(files.get(j).getArrested()==true){
                        populationArrested++;
                    }
                }
                result[0]=(double) populationFrisked/database.get(i).getRecordsForYear().size()*100;
                result[1]=(double) populationArrested/database.get(i).getRecordsForYear().size()*100;
            }
        }

        return result;
    }

    /**
     * This method keeps track of the fraction of Black females, Black males,
     * White females and White males that were stopped for any reason.
     * Drawing out the exact table helps visualize the gender bias.
     * 
     * @param year we are only interested in the records of year.
     * @return a 2D array of percent of number of White and Black females
     *         versus the number of White and Black males.
     */
    public double[][] genderBias ( int year ) {

        double blackPeople=0;
        double whitePeople=0;
        double blackMen=0;
        double whiteMen=0;
        double blackWomen=0;
        double whiteWomen=0;
        for(int i=0; i<database.size();i++){
            if(database.get(i).getcurrentYear()==year){
                ArrayList<SFRecord> recordRace=database.get(i).getRecordsForYear();
                for(int j=0; j<recordRace.size(); j++) {
                    SFRecord rec=recordRace.get(j);
                    if(rec.getRace().equals("B")){
                        blackPeople++;
                    }
                    if (rec.getRace().equals("W")){
                        whitePeople++;
                    }
                    if (rec.getRace().equals("B")&&rec.getGender().equals("F")){
                        blackWomen++;
                    }
                    
                    else if(rec.getRace().equals("W")&&rec.getGender().equals("F")){
                        whiteWomen++;
                    }
                    else if(rec.getRace().equals("B")&&rec.getGender().equals("M")){
                            blackMen++;
                    }
                    else if(rec.getRace().equals("W")&&rec.getGender().equals("M")){
                            whiteMen++;
                    }
                }
            }
               

        }
        double bwperc = (blackWomen/blackPeople) *0.5*100;
        double bmperc = (blackMen/blackPeople) *0.5*100;
        double wwperc = (whiteWomen/whitePeople) *0.5*100;
        double wmperc = (whiteMen/whitePeople) *0.5*100;

        double [][] result={{bwperc, wwperc, bwperc+wwperc},{bmperc, wmperc, bmperc+wmperc}};
        return result;
    }


    /**
     * This method checks to see if there has been increase or decrease 
     * in a certain crime from year 1 to year 2.
     * 
     * Expect year1 to preceed year2 or be equal.
     * 
     * @param crimeDescription
     * @param year1 first year to compare.
     * @param year2 second year to compare.
     * @return 
     */

    public double crimeIncrease ( String crimeDescription, int year1, int year2 ) {
        
        double y1=0;
        double y2=0;

        if (year1>=year2){
            return 0.0;
        }

        for (int i=0; i<database.size();i++){
            int yeartotal = database.get(i).getcurrentYear();
            if (yeartotal==year1&&yeartotal==year2){
                ArrayList<SFRecord> recordYear=database.get(i).getRecordsForYear();
                for(int j=0; j<recordYear.size();j++){
                    if (recordYear.get(i).getDescription().contains(crimeDescription)){
                        if (yeartotal == year1){
                            y1= y1+1;
                        }
                        else{
                            y2=y2+2;
                        }
                    }
                }
            }
        }
            double perch=(((y2-y1)/y1)*100);
            return perch; 
    }
    

    /**
     * This method outputs the NYC borough where the most amount of stops 
     * occurred in a given year. This method will mainly analyze the five 
     * following boroughs in New York City: Brooklyn, Manhattan, Bronx, 
     * Queens, and Staten Island.
     * 
     * @param year we are only interested in the records of year.
     * @return the borough with the greatest number of stops
     */
    public String mostCommonBorough ( int year ) { 
        String [] borough =  new String[5];
        borough[0] = "BROOKLYN";
        borough[1] = "MANHATTAN";
        borough[2] = "BRONX";
        borough[3] = "QUEENS";
        borough[4] = "STATEN ISLAND";
        int[] counts = new int[5];

        for(int i=0; i<database.size();i++){
            if(database.get(i).getcurrentYear()==year){
                for (int j=0; j<database.get(i).getRecordsForYear().size(); j++){
                    SFRecord myRecord = database.get(i).getRecordsForYear().get(j);
                    if (myRecord.getLocation().equalsIgnoreCase(borough[0])){
                        counts[0]++;
                    }
                    else if (myRecord.getLocation().equalsIgnoreCase(borough[1])){
                        counts[1]++;
                    }
                    else if (myRecord.getLocation().equalsIgnoreCase(borough[2])){
                        counts[2]++;
                    }
                    else if (myRecord.getLocation().equalsIgnoreCase(borough[3])){
                        counts[3]++;
                    }
                    else if (myRecord.getLocation().equalsIgnoreCase(borough[4])){
                        counts[4]++;
                    }
                    else if (myRecord.getLocation().equalsIgnoreCase(borough[5])){
                        counts[5]++;
                    }
                }
            }
        }
        int max =0;
        int i = 0;
        for (int d=0; d<counts.length; d++){
            if(counts[d] > max){
            max = counts[d];
            i = d;
            }
        }
        String result = borough[i].substring(0,1)+borough[i].substring(1).toLowerCase();
        return result;
    }
}   
