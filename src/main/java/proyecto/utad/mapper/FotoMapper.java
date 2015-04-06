package proyecto.utad.mapper;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import proyecto.utad.extras.CustomWritable;

public class FotoMapper extends Mapper<Text, Text, Text, CustomWritable> {
		
		public void map(Text k, Text v, Context con)
				throws IOException, InterruptedException {
			String[] line = k.toString().split("\t");
			String url = "";
			if (line.length == 4){
				url = "http://farm"+line[1]+".static.flickr.com/"+line[2]+"/"+line[0]+"_"+line[3]+"_t.jpg"; 
				CustomWritable value = new CustomWritable();
				value.setTipo("foto");
				value.setTexto(url);
				con.write(new Text(line[0]), value); 
			}
		}

}
