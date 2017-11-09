package com.parser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Iterators;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

/**
 * The main entry point for the application that parses a list of zipcodes, and
 * produces the corresponding SLCSP (Second Lowest Cost Silver Plan) rate.
 *
 */
public class SlcspCsvApplication {

	private static String ZIP_FILE_PATH = "zips.csv";
	private static String PLAN_FILE_PATH = "plans.csv";
	private String fileToModifyPath;

	public SlcspCsvApplication(String fileToModify) {
		this.fileToModifyPath = fileToModify;
	}

	public static void main(String[] args) {
		// Check for a modifiable file that contains zip codes needing matching rates
		if (args.length != 1) {
			System.out.println("Please provide only the modifiable CSV file as input.");
			return;
		} else if (!args[0].endsWith(".csv")) {
			System.out.println("Only file type CSV is allowed as input.");
			return;
		}

		try {
			SlcspCsvApplication parser = new SlcspCsvApplication(args[0]);
			parser.updateRates();
			System.out.println("File has been written to : " + args[0]);
		} catch (FileNotFoundException e) {
			System.err.println("Error occurred finding a file.");
			System.err.println(e);
		} catch (IOException e) {
			System.err.println("Error occurred accessing or writing to a file.");
			System.err.println(e);
		} catch (CsvDataTypeMismatchException e) {
			System.err.println("Error occurred converting a file into a POJO.");
			System.err.println(e);
		} catch (CsvRequiredFieldEmptyException e) {
			System.err.println("Error occurred converting a file with required fields: the required fields are empty.");
			System.err.println(e);
		} catch (CsvException e) {
			System.err.println("Error occurred with the given file.");
			System.err.println(e);
		}
	}

	/**
	 * Updates the rates for the given csv file.
	 * 
	 * @throws IOException thrown when something goes wrong reading or writing a resource
	 * @throws CsvException thrown when the modifiable file does not contain zipcode and rate as headers
	 */
	private void updateRates() throws IOException, CsvException {
		// Setting the mapping strategy now so we can check the headers first
		HeaderColumnNameMappingStrategy<SlcspEntity> mappingStrategy =
	            new HeaderColumnNameMappingStrategy<>();
	    mappingStrategy.setType(SlcspEntity.class);
	    getHeaders(mappingStrategy);
		
		// Parse out the list of zips to fill in for
		List<SlcspEntity> modifyList = loadModifiableFile();
		
		// Load the zip map into memory, from only the applicable zips
		Map<String, Set<String>> zipMap = 
				loadZipFile(modifyList.stream()
					.map(record -> record.getZipcode())
					.collect(Collectors.toSet()));
		
		// Load the rate map into memory, from only the applicable plan areas
		Map<String, Set<Double>> planMap = 
				loadPlanFile(zipMap.values()
					.stream()
					.flatMap(Set::stream)
					.collect(Collectors.toSet()));
		
		// Get the second lowest rate for each given zip
		modifyList.forEach(record -> {
			String zip = record.getZipcode();
			Set<String> areaCodes = zipMap.get(zip);
			
			// If the area codes are not null, find the SLCSP rate 
			if (areaCodes != null) {
				// Get the applicable plans for the zipcode's area code
				Map<String, Set<Double>> paredMap = 
					planMap.entrySet()
						.stream()
						.filter(entry -> areaCodes.contains(entry.getKey()))
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
				
				// Get all the rates for the plans, and sort them
				Set<Double> rates = new LinkedHashSet<>();
				paredMap.values()
					.stream()
					.flatMap(Set::stream)
					.sorted()
					.forEachOrdered(rates::add);
				Double rate = null;
				if (rates.size() == 1) {
					rate = rates.iterator().next();
				} else if (rates.size() > 1) {
					// Get the second lowest rate
					rate = Iterators.get(rates.iterator(), 1);
				}
				record.setRate(rate);
			}
		});
		
		// Write it out to the input file
	    FileWriter writer = new FileWriter(fileToModifyPath);
	    StatefulBeanToCsvBuilder<SlcspEntity> builder = new StatefulBeanToCsvBuilder<>(writer);
	    StatefulBeanToCsv<SlcspEntity> beanWriter = builder
	              .withMappingStrategy(mappingStrategy)
	              .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
	              .build();

	    beanWriter.write(modifyList);
		writer.close();
	}
	
	/**
	 * Get the headers of the modifiable file.
	 * 
	 * @param mapping the mapping strategy to set the headers on
	 * @throws IOException thrown when something goes wrong reading or writing a resource
	 * @throws CsvException thrown when the file does not contain zipcode and rate as headers
	 */
	private void getHeaders(HeaderColumnNameMappingStrategy<SlcspEntity> mapping)
			throws IOException, CsvException {
		FileReader fr = new FileReader(fileToModifyPath);
		CSVReader reader = new CSVReader(fr);
		mapping.captureHeader(reader);
		
		// If the headers do not include both zipcode and rate, throw an error
		if (!Arrays.asList(mapping.generateHeader()).containsAll(SlcspEntity.requiredHeaders())) {
			reader.close();
			fr.close();
			throw new CsvException(
				"The modifiable file headers do not contain 'zipcode' and 'rate'. Please format your file with the "
				+ "required headers, and try again.");
		}
				
		reader.close();
		fr.close();
	}
	
	/**
	 * Load the input file into a list of entities.
	 * 
	 * @return a list of {@link SlcspEntity}
	 * @throws IllegalStateException thrown when the builder is incomplete and cannot run methods
	 * @throws IOException thrown when something goes wrong reading a resource
	 */
	private List<SlcspEntity> loadModifiableFile() throws IllegalStateException, IOException {
		FileReader fr = new FileReader(fileToModifyPath);
		List<SlcspEntity> list = new CsvToBeanBuilder<SlcspEntity>(fr)
				.withType(SlcspEntity.class)
				.withOrderedResults(true)
				.build()
				.parse();
		
		fr.close();
		return list;
	}

	/**
	 * Load the zip file containing the corresponding states and rate areas.
	 * 
	 * @param applicableZips the zip codes to get rate areas for
	 * @return a map of zipcode to set of rate areas
	 * @throws IOException thrown when something goes wrong closing the input stream
	 */
	private Map<String, Set<String>> loadZipFile(Set<String> applicableZips) throws IOException {
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(ZIP_FILE_PATH);

		List<ZipCodeEntity> list = new CsvToBeanBuilder<ZipCodeEntity>(new InputStreamReader(is))
				.withType(ZipCodeEntity.class)
				.build()
				.parse();

		Map<String, Set<String>> zipMap = new HashMap<>();

		// Filter for the applicable zipcodes only, and add the mapping
		list.stream()
			.filter(bean -> applicableZips.contains(bean.getZipcode()))
			.forEach(bean -> {
				String zip = bean.getZipcode();
				String stateArea = bean.getStateRateArea();
				
				if (!zipMap.containsKey(zip)) {
					zipMap.put(zip, new HashSet<>());
				}
				
				Set<String> value = zipMap.get(zip);
				value.add(stateArea);
			});
		
		// if a zipcode doesn't have exactly 1 rate area, remove from the list
		zipMap.entrySet().removeIf(entry -> entry.getValue().size() != 1);
		
		is.close();
		return zipMap;
	}

	/**
	 * Load the plan file containing the corresponding rate areas and plan rates.
	 * 
	 * @param applicableAreas the rate areas to get rates for
	 * @return a map of rate area to set of rates
	 * @throws IOException thrown when something goes wrong closing the input stream
	 */
	private Map<String, Set<Double>> loadPlanFile(Set<String> applicableAreas) throws IOException {
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(PLAN_FILE_PATH);

		List<PlanEntity> list = new CsvToBeanBuilder<PlanEntity>(new InputStreamReader(is))
				.withType(PlanEntity.class)
				.build()
				.parse();

		Map<String, Set<Double>> planMap = new HashMap<>();

		// Filter for SILVER plans only and the applicable rate areas, and add the mapping
		list.stream()
			.filter(bean -> bean.getMetal_level().equalsIgnoreCase("Silver"))
			.filter(bean -> applicableAreas.contains(bean.getStateRateArea()))
			.forEach(bean -> {
				Double rate = bean.getRate();
				String stateArea = bean.getStateRateArea();

				if (!planMap.containsKey(stateArea)) {
					planMap.put(stateArea, new HashSet<>());
				}
	
				Set<Double> value = planMap.get(stateArea);
				value.add(rate);
			});
		
		is.close();
		return planMap;
	}

}
