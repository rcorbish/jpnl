package com.rc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import joptsimple.HelpFormatter;
import joptsimple.OptionDescriptor;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

public class Options {

	static final Logger log = LoggerFactory.getLogger( Options.class ) ;

	public static       Path	todaysFile ;
	public static       Path	yesterdaysFile ;
	public static       Path	riskFile ;
	public static       Path	outputFile ;
	
	public static       String 	riskKeys[] ;
	public static       String 	marketKeys[] ;
	public static       String 	outputHeaders[] ;

	public static       String 	riskValue ;
	public static       String 	marketValue ;

	public static       int 	maxErrors ;

	public static		String	marketDelimiter ;
	public static		String	riskDelimiter ;
	public static		String	marketQuoteChar ;
	public static		String	riskQuoteChar ;
	

	public static void init( String args[] ) throws IOException {
	
		OptionParser parser = new OptionParser() ;
		parser.formatHelpWith( new Formatter() ) ;

		OptionSpec<File> co = parser.acceptsAll( asList( "config", "c" )
				, "File containing all options in a properties file" )
				.withRequiredArg().ofType( File.class ) ;

		OptionSpec<File> to = parser.acceptsAll( asList( "t", "today" )
				, "File containing today's market data" )
				.requiredUnless( co )
				.withRequiredArg().ofType( File.class ) ;
		OptionSpec<File> yo = parser.acceptsAll( asList( "y", "yesterday" )
				, "File containing yesterday's market data" )
				.requiredUnless( co )
				.withRequiredArg().ofType( File.class ) ;
		OptionSpec<File> ro = parser.acceptsAll( asList( "r", "risk" )
				, "File containing yesterday's sensitivty data" )
				.requiredUnless( co )
				.withRequiredArg().ofType( File.class ) ;
		OptionSpec<File> oo = parser.acceptsAll( asList( "o", "output" ) 
				, "Output file for P&L" )
				.requiredUnless( co )
				.withRequiredArg().ofType( File.class ) ;

		OptionSpec<String> rko = parser.acceptsAll( asList( "rk", "risk-key" )
				, "Column headers to identify risk factor in risk file" )
				.requiredUnless( co )
				.withRequiredArg() ;
		OptionSpec<String> mko = parser.acceptsAll( asList( "mk", "market-key" )
				, "Column headers to identify risk factor in market data file" )
				.requiredUnless( co )
				.withRequiredArg() ;
		OptionSpec<String> oho = parser.acceptsAll( asList( "oh", "output-headers" ) 
				, "Column headers to print in output file" )
				.requiredUnless( co )
				.withRequiredArg() ;

		OptionSpec<String> rvo = parser.acceptsAll( asList( "rv", "risk-value" )
				, "Column headers to identify value in risk file" )
				.requiredUnless( co )
				.withRequiredArg() ;
		OptionSpec<String> mvo = parser.acceptsAll( asList( "mv", "market-value" )
				, "Column headers to identify value in market data file" )
				.requiredUnless( co )
				.withRequiredArg() ;

		OptionSpec<Integer> eo = parser.acceptsAll( asList( "e", "max-errors" )
				, "Maximum number of errors before aborting processing" )
				.requiredUnless( co )
				.withRequiredArg().ofType( Integer.class ) ;

		OptionSpec<String> mdo = parser.acceptsAll( asList( "md", "market-delimiter" )
				, "Column delimiter in market file" )
				.withRequiredArg().defaultsTo( "|" ) ;

		OptionSpec<String> rdo = parser.acceptsAll( asList( "rd", "risk-delimiter" )
				, "Column delimiter in risk file" )
				.withRequiredArg().defaultsTo( "|" ) ;

		OptionSpec<String> mqo = parser.acceptsAll( asList( "mq", "market-quoteChar" )
				, "Column quote char in market file" )
				.withRequiredArg().defaultsTo( "\"" ) ;

		OptionSpec<String> rqo = parser.acceptsAll( asList( "rq", "risk-quoteChar" )
				, "Column quote char in risk file" )
				.withRequiredArg().defaultsTo( "\"" ) ;


		OptionSpec<Void> ho = parser.acceptsAll( asList( "h", "?", "help" ), "Show this help" ).forHelp() ;
        
        OptionSet options = parser.parse( args ) ;

        if( options.has( ho ) ) {
            parser.printHelpOn( System.out ) ;
            System.exit( 0 ) ;
        }

        if( options.has( co ) ) {
        	List<String> newArgs = new ArrayList<>() ;
        	for( String s : Files.readAllLines( options.valueOf(co).toPath() ) ) {
        		String a[] = s.split( " " ) ;
        		for( String t : a ) {
        			newArgs.add( t ) ;
        		}
        	}
    		options = parser.parse( newArgs.toArray(new String[newArgs.size()] ) ) ;
        } 
        
        todaysFile = options.valueOf( to ).toPath() ; 
        yesterdaysFile = options.valueOf( yo ).toPath() ;
        riskFile = options.valueOf( ro ).toPath() ;
        outputFile = options.valueOf( oo ).toPath() ;
        marketValue = options.valueOf( mvo ) ;
        riskValue = options.valueOf( rvo ) ;
		maxErrors = options.valueOf( eo ) ;
		
		marketDelimiter = options.valueOf( mdo ) ;
		riskDelimiter = options.valueOf( rdo ) ;
		marketQuoteChar = options.valueOf( mqo ) ;
		riskQuoteChar = options.valueOf( rqo ) ;
		
        List<String> tmp = options.valuesOf( rko ) ;
        riskKeys = new String[tmp.size()] ;
        tmp.toArray( riskKeys ) ;

        tmp = options.valuesOf( mko ) ;
        marketKeys = new String[tmp.size()] ;
        tmp.toArray( marketKeys ) ;

        tmp = options.valuesOf( oho ) ;
        outputHeaders= new String[tmp.size()] ;
		tmp.toArray( outputHeaders ) ;        
		
		log.info( "Params" ) ;
		log.info( "todaysFile        : {}", todaysFile ) ;
		log.info( "yesterdaysFile    : {}", yesterdaysFile ) ;
		log.info( "riskFile          : {}", riskFile ) ;
		log.info( "outputFile        : {}", outputFile ) ;
		
		log.info( "riskKeys          : {}", (Object[])riskKeys ) ;
		log.info( "marketKeys        : {}", (Object[])marketKeys ) ;
		log.info( "outputHeaders     : {}", (Object[])outputHeaders ) ;
	
		log.info( "riskValue         : {}", riskValue ) ;
		log.info( "marketValue       : {}", marketValue );
	
		log.info( "maxErrors         : {}", maxErrors ) ;
	
		log.info( "marketDelimiter   : {}", marketDelimiter ) ;
		log.info( "riskDelimiter     : {}", riskDelimiter ) ;
		log.info( "marketQuoteChar   : {}", marketQuoteChar ) ;
		log.info( "riskQuoteChar     : {}", riskQuoteChar ) ;
	
	}
	
	private static List<String> asList( String ...strings ) {
		List<String> rc = new ArrayList<>() ;
		for( String s : strings ) {
			rc.add( s ) ;
		}
		return rc ;
	}
}



class Formatter implements HelpFormatter {

	@Override
	public String format(Map<String, ? extends OptionDescriptor> options ) {
		StringBuilder sb = new StringBuilder() ;
		sb.append( "Arguments & Aliases      Description" ) ;
		sb.append( System.lineSeparator() ) ;
		sb.append( "------------------------|----------------------------" ) ;
		sb.append( System.lineSeparator() ) ;
		for ( OptionDescriptor od : new HashSet<OptionDescriptor>( options.values() ) ) {
			if ( od.representsNonOptions() ) {
				continue ;
			}
			int s = sb.length() ;
			String sep = "" ;
			for( String o : od.options() ) {
				sb.append( sep ) ;
				sep = ", " ;
				sb.append( o ) ;
			}
			int e = sb.length() ;
			
			for( int i=25 ; i>(e-s) ; i-- ) {
				sb.append( ' ' ) ;
			}

			sb.append( od.description() ) ;
			sb.append( od.argumentDescription() ) ;
			sb.append( System.lineSeparator() ) ;
		}
		return sb.toString() ;
	}
}

