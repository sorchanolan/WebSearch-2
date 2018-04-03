package ie.tcd.websearch.util;

import lombok.Data;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;


@Data
public class VectorFieldUtil {

  Field field;

  public VectorFieldUtil(String name, String value) {
    FieldType fieldType = new FieldType();
    fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
    fieldType.setStored(true);
    fieldType.setStoreTermVectors(true);
    fieldType.setStoreTermVectorPositions(true);
    fieldType.setStoreTermVectorOffsets(true);
    fieldType.setStoreTermVectorPayloads(true);
    this.field = new Field(name, value, fieldType);
  }
}
