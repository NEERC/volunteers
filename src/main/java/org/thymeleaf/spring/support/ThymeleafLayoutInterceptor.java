package org.thymeleaf.spring.support;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.Assert;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class ThymeleafLayoutInterceptor extends HandlerInterceptorAdapter {

    private static final String LAYOUT_PREFIX  = "layouts/";
    private static final String DEFAULT_LAYOUT = "public";
    private static final String DEFAULT_VIEW_ATTRIBUTE_NAME = "view";

    private String defaultLayout = DEFAULT_LAYOUT;
    private String layoutPrefix = LAYOUT_PREFIX;
    private String viewAttributeName = DEFAULT_VIEW_ATTRIBUTE_NAME;

    public void setDefaultLayout(final String defaultLayout) {
        Assert.hasLength(defaultLayout);
        this.defaultLayout = defaultLayout;
    }

    public void setLayoutPrefix(final String layoutPrefix) {
        Assert.notNull(layoutPrefix);
        this.layoutPrefix = layoutPrefix;
    }

    public void setViewAttributeName(final String viewAttributeName) {
        Assert.hasLength(viewAttributeName);
        this.viewAttributeName = viewAttributeName;
    }

    /**
     * Builder method to {@link #setViewAttributeName(String)}
     * @param viewAttributeName view attribute name
     * @return this
     */
    public ThymeleafLayoutInterceptor viewAttributeName(final String viewAttributeName) {
        setViewAttributeName(viewAttributeName);
        return this;
    }

    /**
     * Builder method to {@link #setLayoutPrefix(String)}
     * @param layoutPrefix layouts prefix name
     * @return this
     */
    public ThymeleafLayoutInterceptor layoutPrefix(final String layoutPrefix) {
        setLayoutPrefix(layoutPrefix);
        return this;
    }

    /**
     * Builder method to {@link #setDefaultLayout(String)}
     * @param defaultLayout default layout name
     * @return this
     */
    public ThymeleafLayoutInterceptor defaultLayout(final String defaultLayout) {
        setDefaultLayout(defaultLayout);
        return this;
    }

    @Override
    public void postHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler, final ModelAndView modelAndView) throws Exception {
        if (modelAndView == null || !modelAndView.hasView()) {
            return;
        }

        final String originalViewName = modelAndView.getViewName();
        if (isRedirectOrForward(originalViewName)) {
            return;
        }
        final String layoutName = getLayoutName(handler);
        if (Layout.NONE.equals(layoutName)) {
            return;
        }
        modelAndView.setViewName(layoutPrefix + layoutName);
        modelAndView.addObject(this.viewAttributeName, originalViewName);
    }

    private boolean isRedirectOrForward(final String viewName) {
        return viewName.startsWith("redirect:") || viewName.startsWith("forward:");
    }

    private String getLayoutName(final Object handler) {
        if (handler instanceof HandlerMethod) {
            final HandlerMethod handlerMethod = (HandlerMethod) handler;
            final Layout layout = getMethodOrTypeAnnotation(handlerMethod);
            if (layout != null) {
                return layout.value();
            }
        }
        return this.defaultLayout;
    }

    private Layout getMethodOrTypeAnnotation(final HandlerMethod handlerMethod) {
        final Layout layout = handlerMethod.getMethodAnnotation(Layout.class);
        if (layout == null) {
            return handlerMethod.getBeanType().getAnnotation(Layout.class);
        }
        return layout;
    }
}