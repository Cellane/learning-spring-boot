package net.milanvit.learningspringboot;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ImageTests {
    @Test
    public void imagesManagedByLombokShouldWork() {
        Image image = new Image("id", "filename.jpg");

        assertThat(image.getId()).isEqualTo("id");
        assertThat(image.getName()).isEqualTo("filename.jpg");
    }
}
