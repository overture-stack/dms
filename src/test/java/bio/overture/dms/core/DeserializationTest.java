package bio.overture.dms.core;

import static bio.overture.dms.core.util.FileUtils.readResourceStream;
import static bio.overture.dms.util.Tester.assertExceptionThrown;
import static org.junit.jupiter.api.Assertions.assertEquals;

import bio.overture.dms.compose.model.stack.ComposeStack;
import bio.overture.dms.core.util.ObjectSerializer;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class DeserializationTest {

  @Autowired private ObjectSerializer yamlSerializer;

  @Test
  @SneakyThrows
  public void deserialization_noUnknownFields_success(@TempDir Path tempDir) {
    val tempFile = tempDir.resolve("tempFile.yaml").toFile();

    val obj1 = yamlSerializer.deserializeToObject(getConfigYamlFileStream(), ComposeStack.class);

    val jsonNode1 = yamlSerializer.deserialize(getConfigYamlFileStream());
    yamlSerializer.serializeToFile(jsonNode1, tempFile);

    val obj1_1 = yamlSerializer.deserializeToObject(tempFile, ComposeStack.class);
    val obj1_2 = yamlSerializer.convertValue(jsonNode1, ComposeStack.class);

    val jsonString = yamlSerializer.serializeValue(jsonNode1);
    val actualString = Files.readString(tempFile.toPath());
    assertEquals(jsonString, actualString);

    val obj1_3 = yamlSerializer.convertValue(jsonString, ComposeStack.class);

    assertEquals(obj1, obj1_1);
    assertEquals(obj1, obj1_2);
    assertEquals(obj1, obj1_3);
  }

  @Test
  @SneakyThrows
  public void deserialization_unknownFields_fail(@TempDir Path tempDir) {
    val jsonNode = (ObjectNode) yamlSerializer.deserialize(getConfigYamlFileStream());

    jsonNode.put("someRandomField", "someRandomValue");

    val tempFile = tempDir.resolve("tempFile.yaml").toFile();

    yamlSerializer.serializeToFile(jsonNode, tempFile);

    assertExceptionThrown(
        () -> yamlSerializer.deserializeToObject(tempFile, ComposeStack.class),
        UnrecognizedPropertyException.class);

    val jsonString = yamlSerializer.serializeValue(jsonNode);

    assertExceptionThrown(
        () -> yamlSerializer.convertValue(jsonString, ComposeStack.class),
        UnrecognizedPropertyException.class);

    assertExceptionThrown(
        () -> yamlSerializer.convertValue(jsonNode, ComposeStack.class),
        UnrecognizedPropertyException.class);
  }

  @SneakyThrows
  private static InputStream getConfigYamlFileStream() {
    return readResourceStream("example.compose-stack.yaml");
  }
}
