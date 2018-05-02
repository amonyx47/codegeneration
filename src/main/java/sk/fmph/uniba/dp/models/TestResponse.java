package sk.fmph.uniba.dp.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class TestResponse {

    public static int SUCCESS_CODE = 0;
    public static int FAIL_CODE = 1;

    String message;
    int code;
    String sender;

}
