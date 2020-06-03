package top.ericcliu.ds;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author liubi
 * @date 2020-06-02 21:54
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CombinedRes implements SaveToFile {
    private SeedString seed;
    private MLDFScodeString threshold1Res;
    private MLDFScodeString threshold2Res;
    private MLDFScodeString threshold3Res;
    private MLDFScodeString threshold4Res;
    private MLDFScodeString threshold5Res;

    public CombinedRes(SeedString seed) {
        this.seed = seed;
    }

    public MLDFScodeString getThresholdRes(int i) {
        switch (i) {
            case 1:
                return threshold1Res;
            case 2:
                return threshold2Res;
            case 3:
                return threshold3Res;
            case 4:
                return threshold4Res;
            case 5:
                return threshold5Res;
            default:
                throw new IllegalArgumentException(i + "is illegal argument");
        }
    }

    public void setThresholdRes(int i, MLDFScodeString mldfScodeString) {
        switch (i) {
            case 1:
                threshold1Res = mldfScodeString;
                break;
            case 2:
                threshold2Res = mldfScodeString;
                break;
            case 3:
                threshold3Res = mldfScodeString;
                break;
            case 4:
                threshold4Res = mldfScodeString;
                break;
            case 5:
                threshold5Res = mldfScodeString;
                break;
            default:
                throw new IllegalArgumentException(i + "is illegal argument");
        }
    }
}
