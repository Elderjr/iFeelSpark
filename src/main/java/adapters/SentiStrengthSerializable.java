package adapters;

import java.io.Serializable;

import uk.ac.wlv.sentistrength.SentiStrength;

//deixa o SentiStrength serializável para usar o Spark
public class SentiStrengthSerializable extends SentiStrength implements Serializable{

}
