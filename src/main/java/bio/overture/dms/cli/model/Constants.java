package bio.overture.dms.cli.model;

import static bio.overture.dms.compose.model.ComposeServiceResources.SCORE_API;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class Constants {

  public static final String CONFIG_FILE_NAME = "config.yaml";

  public static final class CommandsQuestions {
    public static final String CONFIRM_DESTROY =
        "Are you sure you want to destroy the volumes for all services, all data will be lost and This is IRREVERSIBLE ? ";
  }

  public static final class SongQuestions {
    public static String PASSWORD = "What should the SONG database password be?";
    public static final String PASSWORD_CONFIGURED_SONG_DB = "A password is already configured for SONG db.";
  }

  public static final class DockerImagesConstants {
    public static final String OVERTURE_EGO_UI = "overture/ego-ui";
    public static final String OVERTURE_ARRANGER_UI = "overture/arranger-ui";
    public static final String OVERTURE_DMS_UI = "overture/dms-ui";
    public static final String OVERTURE_ARRANGER_SERVER = "overture/arranger-server";
    public static final String OVERTURE_EGO = "overture/ego";
    public static final String OVERTURE_SONG_SERVER = "overture/song-server";
    public static final String OVERTURE_SCORE_SERVER = "overture/score-server";
    public static final String POSTGRES = "postgres";
    public static final String MINIO_MINIO = "minio/minio";
    public static final String DOCKER_ELASTIC_CO_ELASTICSEARCH_ELASTICSEARCH =
        "docker.elastic.co/elasticsearch/elasticsearch";
    public static final String GHCR_IO_OVERTURE_STACK_DMS_GATEWAY =
        "ghcr.io/overture-stack/dms-gateway";
    public static final String GHCR_IO_OVERTURE_STACK_DMS_GATEWAY_SECURE =
        "ghcr.io/overture-stack/dms-gateway-secure";
    public static final String GHCR_IO_OVERTURE_STACK_MAESTRO = "ghcr.io/overture-stack/maestro";

    public static final String EGO_UI_TAG = "4.2.0";
    public static final String ARRANGER_UI_TAG = "2.12.3";
    public static final String ARRANGER_SERVER_TAG = "2.12.3";
    public static final String SCORE_SERVER_TAG = "5.3.0";
    public static final String EGO_TAG = "4.4.0";
    public static final String SONG_SERVER_TAG = "4.5.0";
    public static final String POSTGRES_TAG = "11.1";
    public static final String MINIO_TAG = "RELEASE.2018-05-11T00-29-24Z";
    public static final String ES_TAG = "7.6.0";
    public static final String DMS_GATEWAY_TAG = "1.0.0";
    public static final String MAESTRO_TAG = "3.8.0";
    public static final String DMS_UI_TAG = "1.0.3";
  }

  public static final class MESSAGES {

    public static final String CHECK_DOCKER_IMAGES_MSG =
        "Checking if docker images exists & downloading missing ones, this can take few minutes depending on the connection speed.. ";
    public static final String CHECK_COMPLETED = "Check completed.";

    public static final String CONFIGURATION_SAVED_MSG =
        "Your configuration file was successfully saved to: /root/.dms/config.yaml\n"
            + "\n"
            + "You may now deploy your configuration to your cluster. For instructions, see:\n"
            + "https://overture.bio/documentation/dms/installation/deploy/\n";
    public static final String PRE_REQ_NOTE =
        "*****************************************************************************************************\n"
            + "!!! NOTE !!!\n"
            + "\n"
            + "   Before starting, make sure you have completed all prerequisite setup steps here:\n"
            + "   https://overture.bio/documentation/dms/installation/configuration/prereq/\n"
            + "\n"
            + "*****************************************************************************************************";

    public static final String POST_DEPLOYMENT_MSG =
        "*****************************************************************************************************\n"
            + "!!! NOTE !!!\n"
            + "\n"
            + "    Before using the DMS platform, please complete post-deployment verification \n"
            + "    and configuration steps required to check the health of your deployment.  For \n"
            + "    instructions, see:\n"
            + "    https://overture.bio/documentation/dms/installation/verify/\n"
            + "\n"
            + "*****************************************************************************************************";
  }

  public static final class GATEWAY {
    public static final String CLUSTER_MODE_TO_CONFIGURE_AND_DEPLOY =
        "Select the cluster mode to configure and deploy: ";
    public static final String GATEWAY_BASE_URL =
        "What is the base DMS Gateway URL (example: https://dms.cancercollaboratory.org)?";
    public static final String SSL_CERT_BASE_PATH =
        "What is the absolute path for the SSL certificate ?";
    public static final String GATEWAY_PORT = "What port will the DMS gateway be exposed on?";
  }

  public static final class GuidesURLS {
    public static final String HELP_HEADER_GUIDE_URLS =
        "\n"
            + "Installation Guide: https://overture.bio/documentation/dms/installation/"
            + "\n"
            + "User Guide: https://overture.bio/documentation/dms/user-guide/"
            + "\n"
            + "\n";
    public static final String GUIDE_DMSUI =
        "Guide: https://overture.bio/documentation/dms/installation/configuration/configure-dms/#configure-dms-ui";
    public static final String GUIDE_MAESTRO =
        "Guide: https://overture.bio/documentation/dms/installation/configuration/configure-dms/#configure-maestro";
    public static final String DEPLOYMENT_MODE =
        "Guide: https://overture.bio/documentation/dms/installation/configuration/#decide-local-or-server-deployment";
    public static final String GUIDE_EGO =
        "Guide: https://overture.bio/documentation/dms/installation/configuration/configure-dms/#configure-ego";
    public static final String GUIDE_SONG =
        "Guide: https://overture.bio/documentation/dms/installation/configuration/configure-dms/#configure-song";
    public static final String GUIDE_SCORE =
        "Guide: https://overture.bio/documentation/dms/installation/configuration/configure-dms/#configure-score";
    public static final String GUIDE_ES =
        "Guide: https://overture.bio/documentation/dms/installation/configuration/configure-dms/#configure-elasticsearch";
  }

  public static final class ScoreQuestions {
    public static final String DEFAULT_SCORE_APP_NAME = SCORE_API.toString();
    public static final String OBJECT_BUCKET_NAME =
        "What is the name of the OBJECT bucket used for SCORE?";
    public static final String STATE_BUCKET_NAME =
        "What is the name of the STATE bucket used for SCORE?";
    public static final String EXISTING_S3_YN =
        "Do you have an existing S3 object storage service you want to use with the SCORE service?";
    public static final String USING_AWS_S3 = "Will you be using AWS S3?";
    public static final String AWS_S3_REGION = "What is the S3 region?";
    public static final String EXT_S3_URL = "What is the URL of the S3 service?";
    public static final String S3_ACCESS_KEY = "What is the S3 access key?";
    public static final String S3_SECRET_KEY = "What is the S3 secret key?";
    public static final String MINIO_CREATE_CREDS =
        "MinIO will be used as the SCORE S3 object storage service. Would you like to automatically create credentials? "
            + "If no, you must enter them in the subsequent questions.";
    public static final String MINIO_ACCESS_KEY = "What should the MinIO access key be?";
    public static final String MINIO_SECRET_KEY = "What should the MinIO secret key be?";
    public static final String YOU_HAVE_ALREADY_CONFIGURED_AN_S3 = "You have already configured an S3 object storage service, do you want to keep the same configurations ?";
  }

  public static final class EgoQuestions {
    public static String API_KEY_DAYS = "How many days should API keys be valid for?";
    public static final String JWT_HOURS_DURATION =
        "How many hours should user-level JWTs be valid for?";
    public static final String APP_JWT_DURATION_HOURS =
        "How many hours should application-level JWTs be valid for?";
    public static final String HOW_MANY_HOURS_SHOULD_REFRESH_TOKENS_BE_VALID_FOR =
        "How many hours should refresh tokens be valid for?";
    public static final String IDENTITY_PROVIDERS =
        "Which OAuth identity providers would you like to enable? e.g: 1,4 ";
    public static final String PASSWORD = "What should the EGO database password be?";
    public static final String WHAT_IS_THE_S_CLIENT_ID = "What is the %s client ID?";
    public static final String WHAT_IS_THE_S_CLIENT_SECRET = "What is the %s client secret?";
    public static final String YOU_HAVE_ALREADY_CONFIGURED_THE_SSO_PROVIDERS = "You have already configured the SSO providers, do you want to keep the same configurations ?";
    public static final String PASSWORD_CONFIGURED_EGO_DB = "A password is already configured for EGO db.";
  }

  @UtilityClass
  public class DmsUiQuestions {
    public String EMAIL =
        "What is the e-mail that your DMS users can contact for support (will appear in the DMS UI)?";
    public String TITLE =
        "Would you like to customize the data portal name (appears in the DMS UI header)?";
    public static final String PROJ_ID =
        "What is the Project ID you will configure in Arranger (to be referenced by DMS UI)?";
    public static final String PROJ_NAME =
        "What is the Project Name you will configure in Arranger (to be referenced by DMS UI)?";
    public static final String DMS_VIZ_TOOL =
            "Which optional visualization tools would you like to install?";
    public static final String ALIAS =
        "What is the Elasticsearch alias name you will configure in Arranger (to be referenced by DMS UI and ALSO must match the alias name previously supplied for Maestro) be? ";
    public static final String ARRANGER_QUESTIONS_NOTE =
        "\n*****************************************************************************************************\n"
            + "!!! NOTE !!!\n"
            + "\n"
            + "    The next 3 fields (Arranger Project ID, Project Name, Elasticsearch AliasName are \n"
            + "    required when you create your project in the Arranger administrative UI after  \n"
            + "    deployment.  The values you use MUST match the ones you supply here for the \n"
            + "    DMS UI configuration. The DMS UI interacts with Arranger and expects the same \n"
            + "    values you input here. For instructions on adding an Arranger project, see:\n"
            + "    https://overture.bio/documentation/dms/installation/verify/#add-project-to-arranger-ui\n"
            + "*****************************************************************************************************\n";
  }

  public static final class MaestroQuestions {
    public static final String PASSWORD_CONFIGURED_ELASTICSEARCH = "A password is already configured for Elasticsearch";

    public static final String ES_PASSWORD =
        "Elasticsearch provides a superuser with default username 'elastic'. What should the superuser's password be?";
    public static String ALIAS =
        "What is the alias of the Elasticsearch index that Maestro will build (must be different from the index name)?";
    public static String INDEX =
        "What is the index name of the Elasticsearch index that Maestro will build (must be different from the alias)?";
  }
}
