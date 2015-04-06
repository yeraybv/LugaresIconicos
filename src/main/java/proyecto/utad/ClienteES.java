package proyecto.utad;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.IOException;

import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.node.Node;

public class ClienteES {

	private String index = null;
	private String type = null;
	private Client client = null;
	private Node node = null;

	
	
	public ClienteES(String index, String type, String clusterName) {
		super();
		this.index = index;
		this.type = type;
		this.node = nodeBuilder().clusterName(clusterName).client(true)
				.node();
		this.client = node.client();
		if (isIndexExist(this.index)) {
			deleteIndex(this.client, this.index);
			createIndex(this.index);
			System.out.println("Indice: " + index + "/" + type + " borrado y creado");
		} else {
			createIndex(this.index);
			System.out.println("Indice: " + index + "/" + type + " nuevo y creado");
		}
	}

	
	private void createIndex(String index) {
		XContentBuilder typemapping = buildJsonMappings();
		XContentBuilder settings = buildJsonSettings();
		
		client.admin().indices()
			.create(new CreateIndexRequest(index).settings(settings).mapping(type,typemapping)).actionGet();

	}

	private void deleteIndex(Client client, String index) {
		try {
			DeleteIndexResponse delete = client.admin().indices()
					.delete(new DeleteIndexRequest(index)).actionGet();
			if (!delete.isAcknowledged()) {
			} else {
			}
		} catch (Exception e) {
		}
	}
	
	
	private XContentBuilder buildJsonSettings() {
		XContentBuilder builder = null;
		try {
			builder = XContentFactory.jsonBuilder();
			builder.startObject()
						.startObject("index")
							.startObject("analysis")
								.startObject("analyzer")
									.startObject("custom_analyser")
										.field("tokenizer", "standard")
										.field("char_filter","html_strip")
										.field("type", "custom")
										.field("filter", new String[] { "lowercase","unique", "max_length", "my_stop" })
									.endObject()
								.endObject()
								.startObject("filter")
									.startObject("my_stop")
										.field("type", "stop")
										.field("stopwords",new String[] { "_english_" })
									.endObject()
								.endObject()
								.startObject("filter")
									.startObject("max_length")
										.field("type", "length")
										.field("max",10)
										.field("min",4)
									.endObject()
								.endObject()
							.endObject()
						.endObject()
					.endObject();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return builder;
	}
	
	private XContentBuilder buildJsonMappings() {
		XContentBuilder builder = null;
		try {

			builder = XContentFactory.jsonBuilder();
			builder.startObject()
						.startObject("_id")
							.field("path", "id")
						.endObject()
						.startObject("_all")
							.field("enabled", true)
							.field("analyzer", "custom_analyser")
						.endObject()
						.startObject("properties")
							.startObject("tags")
								.field("type", "string")
								.field("store", "true")
								.field("index", "analyzed")
								.field("ignore_above", 200)
							.endObject()
							.startObject("descripcion")
								.field("type", "string")
								.field("store", "true")
								.field("index", "analyzed")
								.field("ignore_above", 200)
							.endObject()
							.startObject("foto")
								.field("type", "string")
								.field("store", true)
								.field("index", "not_analyzed")
								.field("include_in_all", false)
							.endObject()
							.startObject("id")
								.field("type", "string")
								.field("store", true)
								.field("index", "not_analyzed")
								.field("include_in_all",false)
							.endObject()
							.startObject("titulo")
								.field("type", "string")
								.field("store", true)
								.field("index", "analyzed")
							.endObject()
							.startObject("ciudad")
								.field("type", "string")
								.field("store", true)
								.field("index", "not_analyzed")
							.endObject()
							.startObject("location")
								.field("type", "geo_point")
								.field("geohash", true)
								.field("geohash_prefix", true)
								.field("include_in_all", false)
								.field("geohash_precision", 10)
								.field("lat_lon", "true")
							.endObject()
						.endObject()
					.endObject();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return builder;
	}


	private boolean isIndexExist(String index) {
		ActionFuture<IndicesExistsResponse> exists = client.admin().indices()
				.exists(new IndicesExistsRequest(index));
		IndicesExistsResponse actionGet = exists.actionGet();
		return actionGet.isExists();
	}

}
