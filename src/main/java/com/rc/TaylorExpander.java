package com.rc;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This parses a file, line by line looking for sensitivity terms
 * If a term is delta or gamma, we find the market data factor
 * for that element and produce the taylor term.
 */
public class TaylorExpander implements AutoCloseable {

	static final Logger log = LoggerFactory.getLogger( TaylorExpander.class ) ;
	
	private final MarketCache marketCache ;
	private final CSVPrinter csvPrinter ;
	private final NumberFormat formatter ;

	private final Charset cs = Charset.forName("UTF8") ;
	
	public TaylorExpander( MarketCache market, Path target ) throws IOException {
		this.marketCache = market ;
		csvPrinter = CSVFormat.EXCEL.withHeader("Tenor", "Currency", "Factor", "Value", "Measure" ).print( target, cs ) ;
		formatter = new DecimalFormat( "0.######" ) ;
	}

	
	public void expandFile( Path path ) throws IOException {

		log.info( "Expanding {}", path ) ;
		CSVFormat psv = CSVFormat.EXCEL
							.withIgnoreEmptyLines()
							.withIgnoreSurroundingSpaces()
							.withFirstRecordAsHeader()
							.withDelimiter('|')
							.withQuote('"' )
							;
		
		int rowCount = 0 ;
		int numErrors = 0 ;
		long start = System.currentTimeMillis() ;
		
		try( BufferedReader br = Files.newBufferedReader(path, cs ) ) {
			Iterable<CSVRecord> records = psv.parse(br);
			for (CSVRecord record : records) {
				rowCount++ ;
				try {
					if( record.get( "Risk Type").equals("Delta") ) {
						processDeltaRecord( record ) ;
					} else if( record.get( "Risk Type").equals("Gamma") ) {
						processGammaRecord( record ) ;
					}
				} catch( Throwable t ) {
					log.error( "Failure in {} at row {}", path, rowCount, t ) ;
					numErrors++ ;
					if( numErrors > 50 ) {
						log.warn( "Too many errors - processing aborted" ) ;
						break ;
					}
				}
			}
		}
		
		float time = ( System.currentTimeMillis() - start ) / 1000.0f ;
		log.info( "Processed {} records in {}S [{} rows/sec]", rowCount, time, rowCount/time ) ;
	}


	private void processDeltaRecord( CSVRecord record ) throws IOException {
		float val = Float.valueOf( record.get("Value") ) ;
		float expansion = val * marketCache.getFirstOrderFactor( record ) ;
		Map<String,String> map = record.toMap() ;
		map.put("Value", formatter.format(expansion) ) ;
		map.put("Risk Type", "Delta P&L" ) ;
		writeOutput( map.values() ) ;
	}


	private void processGammaRecord( CSVRecord record ) throws IOException {
		float val = Float.valueOf( record.get("Value") ) ;
		float expansion = val * marketCache.getSecondOrderFactor( record ) ; 
		Map<String,String> map = record.toMap() ;
		map.put("Value", formatter.format(expansion) ) ;
		map.put("Risk Type", "Gamma P&L" ) ;
		writeOutput( map.values() ) ;
	}

	
	private void writeOutput( Iterable<String> record ) throws IOException {
		csvPrinter.printRecord( record ) ;
	}

	
	public void close() throws IOException {
		log.info( "Closing csv expanded output" ) ;
		csvPrinter.close() ; 
	}	
}
