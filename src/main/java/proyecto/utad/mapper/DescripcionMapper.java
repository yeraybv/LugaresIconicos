package proyecto.utad.mapper;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import proyecto.utad.extras.CustomWritable;

public class DescripcionMapper extends Mapper<Text, Text, Text, CustomWritable> {
		
		public void map(Text k, Text v, Context con)
				throws IOException, InterruptedException {
			String line = v.toString();
			CustomWritable value = new CustomWritable();
			value.setTipo("descripcion");
			value.setTexto(line.toLowerCase());
			con.write(new Text(k), value); 
		}
}
