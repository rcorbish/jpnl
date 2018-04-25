package com.rc;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds precomputed first and second order terms for all
 * market data. The key, to retrieve, is the risk factor.
 */
public class MarketCache {
	
	static final Logger log = LoggerFactory.getLogger( MarketCache.class ) ;

	final private Map<String,Float> today ;
	final private Map<String,Float> yesterday ;
	final private Map<String,Float> firstOrderFactor ;
	final private Map<String,Float> secondOrderFactor ;
	
	final private Charset cs = Charset.forName("UTF8") ;
	
	public MarketCache( Path t, Path y ) throws IOException {
		
		today = readFile(t) ;
		yesterday = readFile(y) ;
		firstOrderFactor = new HashMap<>() ;
		secondOrderFactor = new HashMap<>() ;
		
		for( Map.Entry<String, Float> e : today.entrySet() ) {
			Float yf = yesterday.get( e.getKey() ) ;
			float change = e.getValue()-yf ;
			firstOrderFactor.put( e.getKey(), change ) ;
			secondOrderFactor.put( e.getKey(), change*change*0.5f ) ;
		}
	}

	public String getFactorKey( CSVRecord record ) {
		StringJoiner sj = new StringJoiner( "/" ) ;
		sj.add( record.get("Factor") ) ;
		sj.add( record.get("Tenor") ) ;
		return sj.toString() ;
	}

	public float getFirstOrderFactor( CSVRecord record ) {
		String marketKey = getFactorKey(record) ;
		return getFirstOrderFactor(marketKey) ;
	}

	public float getFirstOrderFactor( String key ) {
		Float f = firstOrderFactor.get( key ) ;
		if( f == null ) {
			throw new RuntimeException( "Cannot find first order factor for " + key ) ;
		}  
		return f ;
	}

	public float getSecondOrderFactor( CSVRecord record ) {
		String marketKey = getFactorKey(record) ;
		return getFirstOrderFactor(marketKey) ;
	}

	public float getSecondOrderFactor( String key ) {
		Float f = secondOrderFactor.get( key ) ;
		if( f == null ) {
			throw new RuntimeException( "Cannot find second order factor for " + key ) ;
		}  
		return f ;
	}

	private Map<String,Float> readFile( Path path ) throws IOException {
		log.info( "Reading market data from {}", path ) ;

		Map<String,Float> rc = new HashMap<>() ;
		
		CSVFormat psv = CSVFormat.EXCEL
							.withIgnoreEmptyLines()
							.withIgnoreSurroundingSpaces()
							.withFirstRecordAsHeader()
							.withDelimiter('|')
							.withQuote('"' )
							;

		try( BufferedReader br = Files.newBufferedReader(path, cs ) ) {
			Iterable<CSVRecord> records = psv.parse(br);
			for (CSVRecord record : records) {
				String key = getFactorKey(record) ;
			    float val = Float.valueOf( record.get("Value") ) ;
			    rc.put( key, val ) ;
			}
		}
		log.info( "Found {} records", rc.size() ) ;
		return rc ;
	}
}
