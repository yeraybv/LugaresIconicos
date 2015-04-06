package proyecto.utad.reducer;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import proyecto.utad.extras.CustomWritable;

public class FlickReducer extends Reducer<Text, CustomWritable, Text, MapWritable>
{
	public void reduce(Text key, Iterable<CustomWritable> text, Context con)
            throws IOException , InterruptedException
    {
       Iterator<CustomWritable> valuesIt = text.iterator();
       boolean completo = false;
       MapWritable mapWritable = new MapWritable();
       while(valuesIt.hasNext()){
    	   CustomWritable cw = valuesIt.next();
    	   if (cw.getTipo().toString().equals("location")){
    		   completo = true;	   
    	   }
    	   mapWritable.put(new Text(cw.getTipo()), new Text(cw.getTexto()));
       }
       mapWritable.put(new Text("id"), key);
       if (completo)
    	  con.write(key, mapWritable);
    }
}
