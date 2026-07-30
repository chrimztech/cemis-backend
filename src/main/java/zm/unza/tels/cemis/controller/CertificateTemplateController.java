package zm.unza.tels.cemis.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zm.unza.tels.cemis.entity.CertificateTemplate;
import zm.unza.tels.cemis.entity.CertificateType;
import zm.unza.tels.cemis.repository.CertificateTemplateRepository;

import java.util.List;

@RestController
@RequestMapping("/certificate-templates")
@RequiredArgsConstructor
public class CertificateTemplateController {

    private final CertificateTemplateRepository templateRepository;

    @GetMapping
    public List<CertificateTemplate> list() {
        return templateRepository.findAll();
    }

    @GetMapping("/{type}")
    public CertificateTemplate get(@PathVariable CertificateType type) {
        return templateRepository.findById(type)
            .orElseGet(() -> CertificateTemplate.builder().certificateType(type).build());
    }

    @PutMapping("/{type}")
    public ResponseEntity<CertificateTemplate> update(
            @PathVariable CertificateType type,
            @RequestBody CertificateTemplate patch) {
        var template = templateRepository.findById(type)
            .orElseGet(() -> CertificateTemplate.builder().certificateType(type).build());
        if (patch.getSignatory1Name()  != null) template.setSignatory1Name(patch.getSignatory1Name());
        if (patch.getSignatory1Title() != null) template.setSignatory1Title(patch.getSignatory1Title());
        if (patch.getSignatory2Name()  != null) template.setSignatory2Name(patch.getSignatory2Name());
        if (patch.getSignatory2Title() != null) template.setSignatory2Title(patch.getSignatory2Title());
        if (patch.getTemplateLayout()  != null) template.setTemplateLayout(patch.getTemplateLayout());
        return ResponseEntity.ok(templateRepository.save(template));
    }
}
