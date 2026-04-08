package com.github.qqrayzqq.cargoflow.elasticsearch.repository;

import com.github.qqrayzqq.cargoflow.elasticsearch.document.AddressDocument;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressSearchRepository extends ElasticsearchRepository<AddressDocument, String> {
    @Query("""
    {
      "multi_match": {
        "query": "?0",
        "type": "bool_prefix",
        "operator": "and",
        "fields": ["fullText", "fullText._2gram", "fullText._3gram"]
      }
    }
    """)
    List<AddressDocument> searchByQuery(String query);
}
