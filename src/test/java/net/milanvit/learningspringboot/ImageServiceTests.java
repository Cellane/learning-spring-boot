package net.milanvit.learningspringboot;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.FileSystemUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ImageServiceTests {
    @Autowired
    ImageService imageService;

    @MockBean
    ImageRepository repository;

    @MockBean(name = "setUp")
    Object ignoreImageServiceCommandLineRunner;

    @Before
    public void setUp() throws IOException {
        FileSystemUtils.deleteRecursively(new File(ImageService.UPLOAD_ROOT));
        Files.createDirectory(Paths.get(ImageService.UPLOAD_ROOT));
    }

    @Test
    public void findAllShouldJustReturnTheFlux() {
        given(repository.findAll())
            .willReturn(Flux.just(
                new Image("1", "alpha.jpg"),
                new Image("2", "bravo.jpg")
            ));

        Flux<Image> images = imageService.findAllImages();

        then(images).isNotNull();

        StepVerifier.create(images)
            .recordWith(ArrayList::new)
            .expectNextCount(2)
            .consumeRecordedWith(results -> {
                then(results)
                    .extracting(Image::getId, Image::getName)
                    .contains(
                        tuple("1", "alpha.jpg"),
                        tuple("2", "bravo.jpg")
                    );
            })
            .verifyComplete();
    }

    @Test
    public void findOneShouldReturnNotYetFetchedUrl() {
        Mono<Resource> image = imageService.findOneImage("alpha.jpg");

        then(image).isNotNull();

        StepVerifier.create(image)
            .expectNextMatches(resource -> {
                then(resource.getDescription()).isEqualTo("URL [file:upload-dir/alpha.jpg]");
                then(resource.exists()).isFalse();
                then(resource instanceof UrlResource).isTrue();

                return true;
            })
            .verifyComplete();
    }

    @Test
    public void createImageShouldWork() {
        FilePart file1 = mock(FilePart.class);
        FilePart file2 = mock(FilePart.class);
        Image alphaImage = new Image("1", "alpha.jpg");
        Image bravoImage = new Image("2", "bravo.jpg");

        given(repository.save(new Image(any(), alphaImage.getName())))
            .willReturn(Mono.just(alphaImage));
        given(repository.save(new Image(any(), bravoImage.getName())))
            .willReturn(Mono.just(bravoImage));
        given(repository.findAll()).willReturn(Flux.just(alphaImage, bravoImage));
        given(file1.filename()).willReturn(alphaImage.getName());
        given(file1.transferTo(any(File.class))).willReturn(Mono.empty());
        given(file2.filename()).willReturn(bravoImage.getName());
        given(file2.transferTo(any(File.class))).willReturn(Mono.empty());

        Mono<Void> done = imageService.createImage(Flux.just(file1, file2));

        StepVerifier.create(done)
            .verifyComplete();
    }

    @Test
    public void deleteImageShouldWork() {
        String imageName = "alpha.jpg";
        Mono<Image> image = Mono.just(new Image("1", imageName));

        given(repository.findByName(any())).willReturn(image);
        given(repository.delete(any(Image.class))).willReturn(Mono.empty());

        Mono<Void> done = imageService.deleteImage(imageName);

        StepVerifier.create(done)
            .verifyComplete();
    }
}
