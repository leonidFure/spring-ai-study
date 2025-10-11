package com.example.springaiapp.domain.advisors;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;

public class ExpansionQueryAdvisor implements BaseAdvisor{

    @Override
    public int getOrder() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getOrder'");
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'before'");
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'after'");
    }

}
