/*
 * Copyright 2014 Brian Roach <roach at mostlyharmless dot net>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.mostlyharmless.jghservice.connector.github;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import net.mostlyharmless.jghservice.resources.ServiceConfig;

/**
 *
 * @author Brian Roach <roach at mostlyharmless dot net>
 */
public class CreateIssue implements GithubCommand<Integer>
{
    @JsonProperty
    protected final String title;
    @JsonProperty
    protected final String body;
    @JsonProperty
    protected final List<String> labels;
    
    @JsonIgnore
    protected final ServiceConfig.Repository repo;
    
    
    protected CreateIssue(Init<?> init)
    {
        this.title = init.title;
        this.body = init.body;
        this.repo = init.repo;
        this.labels = init.labels;
    }
    
    @Override
    public String getRequestMethod()
    {
        return POST;
    }
    
    @Override
    public int getExpectedResponseCode()
    {
        return 201;
    }
    
    @Override
    public URL getUrl() throws MalformedURLException
    {
        return new URL(API_URL_BASE + repo.getGithubOwner() + 
                        "/" + repo.getGithubName() + "/issues");
    }

    @Override
    public String getJson() throws JsonProcessingException 
    {
        return mapper.writeValueAsString(this);
    }
    
    @Override
    public Integer processResponse(String json) throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);
        return root.get("number").asInt();
        
    }
    
    protected abstract static class Init<T extends Init<T>>
    {
        private String title;
        private String body;
        private final List<String> labels = new LinkedList<>();
        private ServiceConfig.Repository repo;
        
        protected abstract T self();
        
        public T withRepository(ServiceConfig.Repository repo)
        {
            this.repo = repo;
            return self();
        }
        
        public T withTitle(String title)
        {
            this.title = title;
            return self();
        }
        
        public T withBody(String body)
        {
            this.body = body;
            return self();
        }
        
        public T addLabel(String label)
        {
            labels.add(label);
            return self();
        }
        
        public T withLabels(List<String> labels)
        {
            this.labels.addAll(labels);
            return self();
        }
        
        protected void validate()
        {
            if (repo == null || title == null)
            {
                throw new IllegalStateException("Must have a repo and a title.");
            }
        }
        
        public CreateIssue build()
        {
            validate();
            return new CreateIssue(this);
        }
        
    }
    
    public static class Builder extends Init<Builder>
    {
        @Override
        protected Builder self()
        {
            return this;
        }
    }
    
}