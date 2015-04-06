package proyecto.utad;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.elasticsearch.hadoop.mr.EsOutputFormat;

import proyecto.utad.extras.CustomWritable;
import proyecto.utad.mapper.DescripcionMapper;
import proyecto.utad.mapper.FotoMapper;
import proyecto.utad.mapper.GeoMapper;
import proyecto.utad.mapper.TagsMapper;
import proyecto.utad.mapper.TituloMapper;
import proyecto.utad.reducer.FlickReducer;

public class MainJob extends Configured implements Tool {

	private static Properties properties = null;
	private static String indexES;
	private static String typeES;
	private static String clusterName;
	
	private static void loadProperties(String fileName) throws IOException
	{
		properties = new Properties();
		try {
			FileInputStream in = new FileInputStream(fileName);
			properties.load(in);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		indexES = properties.getProperty("index_name");
		typeES = properties.getProperty("type_name");
		clusterName = properties.getProperty("clusterName");
	}
	
	
	public int run(String[] args) throws Exception {
		
		System.out.println("MainJob START");
		// Lectura de las properties de configuracion
		String inputIdTitle = properties.getProperty("id_title");
		String inputIdDescripcion = properties.getProperty("id_descripcion");
		String inputIdLatLon = properties.getProperty("id_lat_lon");
		String inputIdFarmServer = properties.getProperty("id_farm_server");
		String inputIdTags = properties.getProperty("id_userid_tags");
		String ip = properties.getProperty("ip");
		String port = properties.getProperty("port");
		
		String numeroReducer = properties.getProperty("numero_reducer");
		
		// Ficheros de entrada
		Path input_1=new Path(inputIdTitle);
		Path input_2=new Path(inputIdDescripcion);
		Path input_3=new Path(inputIdLatLon);
		Path input_4=new Path(inputIdFarmServer);
		Path input_5=new Path(inputIdTags);
			
		// Creamos el job
		@SuppressWarnings("deprecation")
		Job job = new Job(getConf(), "Flickrjob");
		job.setJarByClass(MainJob.class);
		
		Configuration conf = job.getConfiguration();
		conf.set("fs.defaultFS", "hdfs://localhost.localdomain:8020");
		conf.set("key.value.separator.in.input.line", " ");
		conf.set("es.nodes",ip + ":" + port);
        conf.set("es.resource", indexES + "/" + typeES);
        
		// MultipleInputs
		MultipleInputs.addInputPath(job, input_1, KeyValueTextInputFormat.class, TituloMapper.class);
        MultipleInputs.addInputPath(job,input_2, KeyValueTextInputFormat.class, DescripcionMapper.class);
        MultipleInputs.addInputPath(job,input_3, KeyValueTextInputFormat.class, GeoMapper.class);
        MultipleInputs.addInputPath(job,input_4, KeyValueTextInputFormat.class, FotoMapper.class);
        MultipleInputs.addInputPath(job,input_5, KeyValueTextInputFormat.class, TagsMapper.class);
        
        // Output a ES
 		job.setOutputFormatClass(EsOutputFormat.class);
		
		// Salida del mapper
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(CustomWritable.class);
		// Salida del reducer
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(MapWritable.class);
		// Mapper y Reducer
		job.setReducerClass(FlickReducer.class);		
		// Numero de Reducers
		job.setNumReduceTasks(Integer.parseInt(numeroReducer));
		
		job.waitForCompletion(true);
		System.out.println("MainJob FINISH SUCCESS");
		
		return 1;
	}
	
	
	public static void main(String args[]) throws Exception {
		
		if (args.length != 1) {
			System.out.printf("Uso: <config.properties file>\n");
			System.exit(-1);
		}
		loadProperties(args[0]);
		System.out.println("Propiedades cargadas");
		new ClienteES(indexES, typeES, clusterName);
		ToolRunner.run(new MainJob(), args);
		System.exit(1);
	}

}

