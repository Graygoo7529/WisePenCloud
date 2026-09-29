package com.oriole.wisepen.document.api.domain.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentPreviewImageResponse {
    private String resourceId;
    private String documentId;
    private Integer version;
    private String objectKey;
    private String markdownLink;
    private int sourcePageNo;
    /** 当前为 TEXT：第一页提取文本的排版图，不是版式截图。 */
    private String previewKind;
}

