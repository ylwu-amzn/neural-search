/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.neuralsearch.query.ext;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.opensearch.core.common.io.stream.StreamInput;
import org.opensearch.core.common.io.stream.StreamOutput;
import org.opensearch.core.xcontent.XContentBuilder;
import org.opensearch.core.xcontent.XContentParser;
import org.opensearch.search.SearchExtBuilder;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Holds ext data from the query for reranking processors. Since
 * there can be multiple kinds of rerank processors with different
 * contexts, all we can assume is that there's keys and objects.
 * e.g. ext might look like
 * {
 *   "query": {blah},
 *   "ext": {
 *     "rerank": {
 *       "query_context": {
 *         "query_text": "some question to rerank about"
 *       }
 *     }
 *   }
 * }
 * or
 * {
 *   "query": {blah},
 *   "ext": {
 *     "rerank": {
 *       "query_context": {
 *         "query_path": "query.neural.embedding.query_text"
 *       }
 *     }
 *   }
 * }
 */
@AllArgsConstructor
public class RerankSearchExtBuilder extends SearchExtBuilder {

    public final static String PARAM_FIELD_NAME = "rerank";
    @Getter
    protected Map<String, Object> params;

    public RerankSearchExtBuilder(StreamInput in) throws IOException {
        params = in.readMap();
    }

    @Override
    public String getWriteableName() {
        return PARAM_FIELD_NAME;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeMap(params);
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        for (String key : this.params.keySet()) {
            builder.field(key, this.params.get(key));
        }
        return builder;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getClass(), this.params);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof RerankSearchExtBuilder) && params.equals(((RerankSearchExtBuilder) obj).params);
    }

    /**
     * Pick out the first RerankSearchExtBuilder from a list of SearchExtBuilders
     * @param builders list of SearchExtBuilders
     * @return the RerankSearchExtBuilder
     */
    public static RerankSearchExtBuilder fromExtBuilderList(List<SearchExtBuilder> builders) {
        Optional<SearchExtBuilder> b = builders.stream().filter(bldr -> bldr instanceof RerankSearchExtBuilder).findFirst();
        if (b.isPresent()) {
            return (RerankSearchExtBuilder) b.get();
        } else {
            return null;
        }
    }

    /**
     * Parse XContent to rerankSearchExtBuilder
     * @param parser parser parsing this searchExt
     * @return RerankSearchExtBuilder represented by this searchExt
     * @throws IOException if problems parsing
     */
    public static RerankSearchExtBuilder parse(XContentParser parser) throws IOException {
        RerankSearchExtBuilder ans = new RerankSearchExtBuilder((Map<String, Object>) parser.map());
        return ans;
    }

}
