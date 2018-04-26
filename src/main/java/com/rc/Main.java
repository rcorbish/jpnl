package com.rc;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processes a risk file using Taylor Expansion to determine future 
 * value. Read two days of markets, compute the difference then, for
 * each risk type calculate a PV change.
 * 
 * Delta PV = mkt-change x delta-risk
 * Gamma PV = mkt-change ^ mkt-change * gamma-risk / 2.0
 * 
 */
public class Main {

	static final Logger log = LoggerFactory.getLogger( Main.class ) ;

	public static void main( String args[] ) {
		try {
			Options.init( args ) ;
			Main self = new Main() ;
			self.process();
		} catch( Throwable t ) {
			t.printStackTrace(); 
			System.exit( 1 ) ;
		}
	}

	
	//
	// Do the actual processing
	//
	// Read in two days of mkt data and compute
	// factors for first and second order terms for
	// each mkt data entry
	//
	// Then scan through a risk file to get first
	// and second order sensitivities, use each of
	// those to determine the change due to underlying
	// shift 
	//
	private void process() throws IOException {
		log.info( "Processing" ) ;
		
		MarketCache market = new MarketCache() ;
		
		try( TaylorExpander expander = new TaylorExpander( market ) ) {
			expander.expandFile( Options.riskFile ) ;
		}
	}
}
