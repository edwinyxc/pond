package pond.web.restful;

/**
 * Created by edwin on 3/12/2017.
 */
enum CollectionFormatEnum {
    CSV("csv"),
    SSV("ssv"),
    TSV("tsv"),
    PIPES("pipes");

    final String val;

    CollectionFormatEnum(String val) {
        this.val = val;
    }
}
