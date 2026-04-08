package com.github.qqrayzqq.cargoflow.elasticsearch.document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "addresses")
@Getter
@Setter
@NoArgsConstructor
public class AddressDocument {
    @Id
    private String id;

    @Field(type = FieldType.Search_As_You_Type)
    private String city;

    @Field(type = FieldType.Search_As_You_Type)
    private String street;

    @Field(type = FieldType.Search_As_You_Type)
    private String zip;

    @Field(type = FieldType.Keyword)
    private String country;

    @Field(type = FieldType.Keyword)
    private String buildingNumber;

    @Field(type = FieldType.Search_As_You_Type)
    private String fullText;

    public AddressDocument(String id, String city, String street, String zip, String country, String buildingNumber) {
        this.id = id;
        this.city = city;
        this.street = street;
        this.zip = zip;
        this.country = country;
        this.buildingNumber = buildingNumber;
        this.fullText = city + " " + street + " " + buildingNumber + " " + zip + " " + country;
    }
}
