package org.canova.cli.subcommands;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.canova.cli.csv.schema.CSVInputSchema;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Vectorize implements SubCommand {
	  private static final Logger log = LoggerFactory.getLogger(Vectorize.class);
	  protected String[] args;
	  public String configurationFile = "";
	public Properties configProps = null;
	
	private CSVInputSchema inputSchema = new CSVInputSchema();


	// this picks up the input schema file from the properties file and loads it
	private void loadInputSchemaFile() {

		this.inputSchema = null;

	}



	// picked up in the command line parser flags (-conf=<foo.txt>)
	public void loadConfigFile() {

		this.configProps = new Properties();
		
		//Properties prop = new Properties();
		InputStream in = null;
		try {
			in = new FileInputStream( this.configurationFile );
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			this.configProps.load(in);
			in.close();	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			

	}

	// 1. load conf file
	// 2, load schema file
	// 3. transform csv -> output format
	public void executeVectorizeWorkflow() {

		
		// load stuff (conf, schema) --> CSVInputSchema
		
		this.loadConfigFile();
		
		this.loadInputSchemaFile();
		
		// collect dataset statistics --> CSVInputSchema
		
			// [ first dataset pass ]
			// for each row in CSV Dataset
		
		// generate dataset report --> DatasetSummaryStatistics
		
		// produce converted/vectorized output based on statistics --> Transforms + CSVInputSchema + Rows

			// [ second dataset pass ]		
	}


	  /**
	   * @param args arguments for command
	   */
	  public Vectorize(String[] args) {
	    this.args = args;
	    CmdLineParser parser = new CmdLineParser(this);
	    try {
	      parser.parseArgument(args);
	    } catch (CmdLineException e) {
	      parser.printUsage(System.err);
	      log.error("Unable to parse args", e);
	    }

	  }
}