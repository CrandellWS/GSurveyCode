////
////  ========================================================================
////  Copyright (c) 1995-2015 Mort Bay Consulting Pty. Ltd.
////  ------------------------------------------------------------------------
////  All rights reserved. This program and the accompanying materials
////  are made available under the terms of the Eclipse Public License v1.0
////  and Apache License v2.0 which accompanies this distribution.
////
////      The Eclipse Public License is available at
////      http://www.eclipse.org/legal/epl-v10.html
////
////      The Apache License v2.0 is available at
////      http://www.opensource.org/licenses/apache2.0.php
////
////  You may elect to redistribute this code under either of these licenses.
////  ========================================================================
////
//
//package videoworker.servlet;
//
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.net.URL;
//import java.nio.ByteBuffer;
//import java.util.Enumeration;
//import java.util.List;
//
//import javax.servlet.AsyncContext;
//import javax.servlet.RequestDispatcher;
//import javax.servlet.ServletContext;
//import javax.servlet.ServletException;
//import javax.servlet.UnavailableException;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import org.eclipse.jetty.http.HttpContent;
//import org.eclipse.jetty.http.HttpField;
//import org.eclipse.jetty.http.HttpFields;
//import org.eclipse.jetty.http.HttpGenerator.CachedHttpField;
//import org.eclipse.jetty.http.HttpHeader;
//import org.eclipse.jetty.http.HttpMethod;
//import org.eclipse.jetty.http.MimeTypes;
//import org.eclipse.jetty.io.WriterOutputStream;
//import org.eclipse.jetty.server.HttpOutput;
//import org.eclipse.jetty.server.InclusiveByteRange;
//import org.eclipse.jetty.server.ResourceCache;
//import org.eclipse.jetty.server.Response;
//import org.eclipse.jetty.server.handler.ContextHandler;
//import org.eclipse.jetty.util.BufferUtil;
//import org.eclipse.jetty.util.Callback;
//import org.eclipse.jetty.util.IO;
//import org.eclipse.jetty.util.MultiPartOutputStream;
//import org.eclipse.jetty.util.QuotedStringTokenizer;
//import org.eclipse.jetty.util.URIUtil;
//import org.eclipse.jetty.util.log.Log;
//import org.eclipse.jetty.util.log.Logger;
//import org.eclipse.jetty.util.resource.Resource;
//import org.eclipse.jetty.util.resource.ResourceFactory;
//
//
//
///* ------------------------------------------------------------ */
///** The default servlet.
// *
// * This servlet, normally mapped to /, provides the handling for static
// * content, OPTION and TRACE methods for the context.
// * The following initParameters are supported, these can be set either
// * on the servlet itself or as ServletContext initParameters with a prefix
// * of org.eclipse.jetty.servlet.Default. :
// * <PRE>
// *  acceptRanges      If true, range requests and responses are
// *                    supported
// *
// *  dirAllowed        If true, directory listings are returned if no
// *                    welcome file is found. Else 403 Forbidden.
// *
// *  welcomeServlets   If true, attempt to dispatch to welcome files
// *                    that are servlets, but only after no matching static
// *                    resources could be found. If false, then a welcome
// *                    file must exist on disk. If "exact", then exact
// *                    servlet matches are supported without an existing file.
// *                    Default is true.
// *
// *                    This must be false if you want directory listings,
// *                    but have index.jsp in your welcome file list.
// *
// *  redirectWelcome   If true, welcome files are redirected rather than
// *                    forwarded to.
// *
// *  gzip              If set to true, then static content will be served as
// *                    gzip content encoded if a matching resource is
// *                    found ending with ".gz"
// *
// *  resourceBase      Set to replace the context resource base
// *
// *  resourceCache     If set, this is a context attribute name, which the servlet
// *                    will use to look for a shared ResourceCache instance.
// *
// *  relativeResourceBase
// *                    Set with a pathname relative to the base of the
// *                    servlet context root. Useful for only serving static content out
// *                    of only specific subdirectories.
// *
// *  pathInfoOnly      If true, only the path info will be applied to the resourceBase
// *
// *  stylesheet	      Set with the location of an optional stylesheet that will be used
// *                    to decorate the directory listing html.
// *
// *  etags             If True, weak etags will be generated and handled.
// *
// *  maxCacheSize      The maximum total size of the cache or 0 for no cache.
// *  maxCachedFileSize The maximum size of a file to cache
// *  maxCachedFiles    The maximum number of files to cache
// *
// *  useFileMappedBuffer
// *                    If set to true, it will use mapped file buffer to serve static content
// *                    when using NIO connector. Setting this value to false means that
// *                    a direct buffer will be used instead of a mapped file buffer.
// *                    This is set to false by default by this class, but may be overridden
// *                    by eg webdefault.xml
// *
// *  cacheControl      If set, all static content will have this value set as the cache-control
// *                    header.
// *
// * otherGzipFileExtensions
// *                    Other file extensions that signify that a file is gzip compressed. Eg ".svgz"
// *
// *
// * </PRE>
// *
// *
// *
// *
// */
//public class StreamServlet extends HttpServlet implements ResourceFactory
//{
//    private static final Logger LOG = Log.getLogger(StreamServlet.class);
//
//    private static final long serialVersionUID = 4930458713846881193L;
//
//    private static final CachedHttpField ACCEPT_RANGES = new CachedHttpField(HttpHeader.ACCEPT_RANGES, "bytes");
//    private final String rb;
//
//    private ServletContext _servletContext;
//    private ContextHandler _contextHandler;
//
//    private boolean _acceptRanges=true;
//    private boolean _pathInfoOnly=true;
//    private boolean _etags=false;
//    private String cc="max-age=3600,public";
//
//    private Resource _resourceBase;
//    private ResourceCache _cache;
//
//    private MimeTypes _mimeTypes;
//    private boolean _useFileMappedBuffer=false;
//    private HttpField _cacheControl;
//    private int max_cache_size=-1;
//    private int max_cached_file_size=-2;
//    private int max_cached_files=-2;
//
//
//    public StreamServlet(String rb)
//    {
//        this.rb=rb;
//    }
//
//    /* ------------------------------------------------------------ */
//    @Override
//    public void init()
//            throws UnavailableException
//    {
//        _servletContext=getServletContext();
//        _contextHandler = initContextHandler(_servletContext);
//        _mimeTypes = new MimeTypes();
//        _mimeTypes.addMimeMapping("mp4","video/mp4");
//        _mimeTypes.addMimeMapping("png","image/png");
//
//        try{_resourceBase=_contextHandler.newResource(rb);}
//        catch (Exception e)
//        {
//            LOG.warn(Log.EXCEPTION,e);
//            throw new UnavailableException(e.toString());
//        }
//        _cacheControl=new CachedHttpField(HttpHeader.CACHE_CONTROL, cc);
//
//        try
//        {
//            if (_cache==null && (max_cached_files!=-2 || max_cache_size!=-2 || max_cached_file_size!=-2))
//            {
//                _cache= new ResourceCache(null,this,_mimeTypes,_useFileMappedBuffer,_etags);
//
//                if (max_cache_size>=0)
//                    _cache.setMaxCacheSize(max_cache_size);
//                if (max_cached_file_size>=-1)
//                    _cache.setMaxCachedFileSize(max_cached_file_size);
//                if (max_cached_files>=-1)
//                    _cache.setMaxCachedFiles(max_cached_files);
//            }
//        }
//        catch (Exception e)
//        {
//            LOG.warn(Log.EXCEPTION,e);
//            throw new UnavailableException(e.toString());
//        }
//        if (LOG.isDebugEnabled())
//            LOG.debug("resource base = "+_resourceBase);
//    }
//
//    /**
//     * Compute the field _contextHandler.<br/>
//     * In the case where the StreamServlet is deployed on the HttpService it is likely that
//     * this method needs to be overwritten to unwrap the ServletContext facade until we reach
//     * the original jetty's ContextHandler.
//     * @param servletContext The servletContext of this servlet.
//     * @return the jetty's ContextHandler for this servletContext.
//     */
//    protected ContextHandler initContextHandler(ServletContext servletContext)
//    {
//        ContextHandler.Context scontext=ContextHandler.getCurrentContext();
//        if (scontext==null)
//        {
//            if (servletContext instanceof ContextHandler.Context)
//                return ((ContextHandler.Context)servletContext).getContextHandler();
//            else
//                throw new IllegalArgumentException("The servletContext " + servletContext + " " +
//                        servletContext.getClass().getName() + " is not " + ContextHandler.Context.class.getName());
//        }
//        else
//            return ContextHandler.getCurrentContext().getContextHandler();
//    }
//
//
//    /* ------------------------------------------------------------ */
//    /** get Resource to serve.
//     * Map a path to a resource. The default implementation calls
//     * HttpContext.getResource but derived servlets may provide
//     * their own mapping.
//     * @param pathInContext The path to find a resource for.
//     * @return The resource to serve.
//     */
//    @Override
//    public Resource getResource(String pathInContext)
//    {
//        Resource r=null;
//        try
//        {
//            if (_resourceBase!=null)
//            {
//                r = _resourceBase.addPath(pathInContext);
//                if (!_contextHandler.checkAlias(pathInContext,r))
//                    r=null;
//            }
//            else if (_servletContext instanceof ContextHandler.Context)
//            {
//                r = _contextHandler.getResource(pathInContext);
//            }
//            else
//            {
//                URL u = _servletContext.getResource(pathInContext);
//                r = _contextHandler.newResource(u);
//            }
//
//            if (LOG.isDebugEnabled())
//                LOG.debug("Resource "+pathInContext+"="+r);
//        }
//        catch (IOException e)
//        {
//            LOG.ignore(e);
//        }
//
//        return r;
//    }
//
//    /* ------------------------------------------------------------ */
//    @Override
//    protected void doGet(HttpServletRequest request, HttpServletResponse response)
//            throws ServletException, IOException
//    {
//
//        //The following are CORS headers. Max age informs the
//        //browser to keep the results of this call for 1 day.
//        response.setHeader("Access-Control-Allow-Origin", "*");
//        response.setHeader("Access-Control-Allow-Methods", "GET");
//        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
//        response.setHeader("Access-Control-Max-Age", "86400");
//        //Tell the browser what requests we allow.
//        response.setHeader("Allow", "GET, HEAD, POST, TRACE, OPTIONS");
//
//        String servletPath=null;
//        String pathInfo=null;
//        Enumeration<String> reqRanges = null;
//        Boolean included =request.getAttribute(RequestDispatcher.INCLUDE_REQUEST_URI)!=null;
//        if (included!=null && included.booleanValue())
//        {
//            servletPath=(String)request.getAttribute(RequestDispatcher.INCLUDE_SERVLET_PATH);
//            pathInfo=(String)request.getAttribute(RequestDispatcher.INCLUDE_PATH_INFO);
//            if (servletPath==null)
//            {
//                servletPath=request.getServletPath();
//                pathInfo=request.getPathInfo();
//            }
//        }
//        else
//        {
//            included = Boolean.FALSE;
//            servletPath = _pathInfoOnly?"/":request.getServletPath();
//            pathInfo = request.getPathInfo();
//
//            // Is this a Range request?
//            reqRanges = request.getHeaders(HttpHeader.RANGE.asString());
//            if (!hasDefinedRange(reqRanges))
//                reqRanges = null;
//        }
//
//        String pathInContext=URIUtil.addPaths(servletPath,pathInfo);
//        boolean endsWithSlash=(pathInfo==null?request.getServletPath():pathInfo).endsWith(URIUtil.SLASH);
//
//        // Find the resource and content
//        Resource resource=null;
//        HttpContent content=null;
//        boolean close_content=true;
//        try
//        {
//            if (_cache==null)
//                resource=getResource(pathInContext);
//            else
//            {
//                content=_cache.lookup(pathInContext);
//                resource=content==null?null:content.getResource();
//            }
//
//            if (LOG.isDebugEnabled())
//                LOG.debug(String.format("uri=%s, resource=%s, content=%s",request.getRequestURI(),resource,content));
//
//            // Handle resource
//            if (resource==null || !resource.exists())
//            {
//                if (included)
//                    throw new FileNotFoundException("!" + pathInContext);
//                response.sendError(HttpServletResponse.SC_NOT_FOUND);
//            }
//            else if (!resource.isDirectory())
//            {
//                if (endsWithSlash && pathInContext.length()>1)
//                {
//                    String q=request.getQueryString();
//                    pathInContext=pathInContext.substring(0,pathInContext.length()-1);
//                    if (q!=null&&q.length()!=0)
//                        pathInContext+="?"+q;
//                    response.sendRedirect(response.encodeRedirectURL(URIUtil.addPaths(_servletContext.getContextPath(),pathInContext)));
//                }
//                else
//                {
//                    // ensure we have content
//                    if (content==null)
//                        content=new HttpContent.ResourceAsHttpContent(resource,
//                                _mimeTypes.getMimeByExtension(resource.toString()),
//                                response.getBufferSize(),_etags);
//
//                    if (included.booleanValue() || passConditionalHeaders(request,response, resource,content))
//                        close_content=sendData(request,response,included.booleanValue(),resource,content,reqRanges);
//                }
//            }
//            else
//            {
//                response.sendError(HttpServletResponse.SC_NOT_FOUND);
//            }
//        }
//        catch(IllegalArgumentException e)
//        {
//            LOG.warn(Log.EXCEPTION,e);
//            if(!response.isCommitted())
//                response.sendError(500, e.getMessage());
//        }
//        finally
//        {
//            if (close_content)
//            {
//                if (content!=null)
//                    content.release();
//                else if (resource!=null)
//                    resource.close();
//            }
//        }
//    }
//
//    /* ------------------------------------------------------------ */
//    private boolean hasDefinedRange(Enumeration<String> reqRanges)
//    {
//        return (reqRanges!=null && reqRanges.hasMoreElements());
//    }
//
//    /* ------------------------------------------------------------ */
//    @Override
//    protected void doPost(HttpServletRequest request, HttpServletResponse response)
//            throws ServletException, IOException
//    {
//        doGet(request,response);
//    }
//
//    /* ------------------------------------------------------------ */
//    /* (non-Javadoc)
//     * @see javax.servlet.http.HttpServlet#doTrace(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
//     */
//    @Override
//    protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
//    {
//        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
//    }
//
//    /* ------------------------------------------------------------ */
//    @Override
//    protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
//            throws ServletException, IOException
//    {
//        //The following are CORS headers. Max age informs the
//        //browser to keep the results of this call for 1 day.
//        resp.setHeader("Access-Control-Allow-Origin", "*");
//        resp.setHeader("Access-Control-Allow-Methods", "GET");
//        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
//        resp.setHeader("Access-Control-Max-Age", "86400");
//        //Tell the browser what requests we allow.
//        resp.setHeader("Allow", "GET, HEAD, POST, TRACE, OPTIONS");
//
//    }
//
//    /* ------------------------------------------------------------ */
//    /* Check modification date headers.
//     */
//    protected boolean passConditionalHeaders(HttpServletRequest request,HttpServletResponse response, Resource resource, HttpContent content)
//            throws IOException
//    {
//        try
//        {
//            if (!HttpMethod.HEAD.is(request.getMethod()))
//            {
//                if (_etags)
//                {
//                    String ifm=request.getHeader(HttpHeader.IF_MATCH.asString());
//                    if (ifm!=null)
//                    {
//                        boolean match=false;
//                        if (content.getETag()!=null)
//                        {
//                            QuotedStringTokenizer quoted = new QuotedStringTokenizer(ifm,", ",false,true);
//                            while (!match && quoted.hasMoreTokens())
//                            {
//                                String tag = quoted.nextToken();
//                                if (content.getETag().equals(tag))
//                                    match=true;
//                            }
//                        }
//
//                        if (!match)
//                        {
//                            response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
//                            return false;
//                        }
//                    }
//
//                    String if_non_match_etag=request.getHeader(HttpHeader.IF_NONE_MATCH.asString());
//                    if (if_non_match_etag!=null && content.getETag()!=null)
//                    {
//                        // Look for GzipFiltered version of etag
//                        if (content.getETag().equals(request.getAttribute("o.e.j.s.GzipFilter.ETag")))
//                        {
//                            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
//                            response.setHeader(HttpHeader.ETAG.asString(),if_non_match_etag);
//                            return false;
//                        }
//
//                        // Handle special case of exact match.
//                        if (content.getETag().equals(if_non_match_etag))
//                        {
//                            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
//                            response.setHeader(HttpHeader.ETAG.asString(),content.getETag());
//                            return false;
//                        }
//
//                        // Handle list of tags
//                        QuotedStringTokenizer quoted = new QuotedStringTokenizer(if_non_match_etag,", ",false,true);
//                        while (quoted.hasMoreTokens())
//                        {
//                            String tag = quoted.nextToken();
//                            if (content.getETag().equals(tag))
//                            {
//                                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
//                                response.setHeader(HttpHeader.ETAG.asString(),content.getETag());
//                                return false;
//                            }
//                        }
//
//                        // If etag requires content to be served, then do not check if-modified-since
//                        return true;
//                    }
//                }
//
//                // Handle if modified since
//                String ifms=request.getHeader(HttpHeader.IF_MODIFIED_SINCE.asString());
//                if (ifms!=null)
//                {
//                    //Get jetty's Response impl
//                    String mdlm=content.getLastModified();
//                    if (mdlm!=null && ifms.equals(mdlm))
//                    {
//                        response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
//                        if (_etags)
//                            response.setHeader(HttpHeader.ETAG.asString(),content.getETag());
//                        response.flushBuffer();
//                        return false;
//                    }
//
//                    long ifmsl=request.getDateHeader(HttpHeader.IF_MODIFIED_SINCE.asString());
//                    if (ifmsl!=-1 && resource.lastModified()/1000 <= ifmsl/1000)
//                    {
//                        response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
//                        if (_etags)
//                            response.setHeader(HttpHeader.ETAG.asString(),content.getETag());
//                        response.flushBuffer();
//                        return false;
//                    }
//                }
//
//                // Parse the if[un]modified dates and compare to resource
//                long date=request.getDateHeader(HttpHeader.IF_UNMODIFIED_SINCE.asString());
//                if (date!=-1 && resource.lastModified()/1000 > date/1000)
//                {
//                    response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
//                    return false;
//                }
//
//            }
//        }
//        catch(IllegalArgumentException iae)
//        {
//            if(!response.isCommitted())
//                response.sendError(400, iae.getMessage());
//            throw iae;
//        }
//        return true;
//    }
//
//    /* ------------------------------------------------------------ */
//    protected boolean sendData(HttpServletRequest request,
//                               HttpServletResponse response,
//                               boolean include,
//                               Resource resource,
//                               final HttpContent content,
//                               Enumeration<String> reqRanges)
//            throws IOException
//    {
//        final long content_length = (content==null)?resource.length():content.getContentLength();
//
//        // Get the output stream (or writer)
//        OutputStream out =null;
//        boolean written;
//        try
//        {
//            out = response.getOutputStream();
//
//            // has a filter already written to the response?
//            written = out instanceof HttpOutput
//                    ? ((HttpOutput)out).isWritten()
//                    : true;
//        }
//        catch(IllegalStateException e)
//        {
//            out = new WriterOutputStream(response.getWriter());
//            written=true; // there may be data in writer buffer, so assume written
//        }
//
//        if (LOG.isDebugEnabled())
//            LOG.debug(String.format("sendData content=%s out=%s async=%b",content,out,request.isAsyncSupported()));
//
//        if ( reqRanges == null || !reqRanges.hasMoreElements() || content_length<0)
//        {
//            //  if there were no ranges, send entire entity
//            if (include)
//            {
//                resource.writeTo(out,0,content_length);
//            }
//            // else if we can't do a bypass write because of wrapping
//            else if (content==null || written || !(out instanceof HttpOutput))
//            {
//                // write normally
//                writeHeaders(response,content,written?-1:content_length);
//                ByteBuffer buffer = (content==null)?null:content.getIndirectBuffer();
//                if (buffer!=null)
//                    BufferUtil.writeTo(buffer,out);
//                else
//                    resource.writeTo(out,0,content_length);
//            }
//            // else do a bypass write
//            else
//            {
//                // write the headers
//                if (response instanceof Response)
//                {
//                    Response r = (Response)response;
//                    writeOptionHeaders(r.getHttpFields());
//                    r.setHeaders(content);
//                }
//                else
//                    writeHeaders(response,content,content_length);
//
//                // write the content asynchronously if supported
//                if (request.isAsyncSupported())
//                {
//                    final AsyncContext context = request.startAsync();
//                    context.setTimeout(0);
//
//                    ((HttpOutput)out).sendContent(content,new Callback()
//                    {
//                        @Override
//                        public void succeeded()
//                        {
//                            context.complete();
//                            content.release();
//                        }
//
//                        @Override
//                        public void failed(Throwable x)
//                        {
//                            if (x instanceof IOException)
//                                LOG.debug(x);
//                            else
//                                LOG.warn(x);
//                            context.complete();
//                            content.release();
//                        }
//
//                        @Override
//                        public String toString()
//                        {
//                            return String.format("StreamServlet@%x$CB", StreamServlet.this.hashCode());
//                        }
//                    });
//                    return false;
//                }
//                // otherwise write content blocking
//                ((HttpOutput)out).sendContent(content);
//
//            }
//        }
//        else
//        {
//            // Parse the satisfiable ranges
//            List<InclusiveByteRange> ranges =InclusiveByteRange.satisfiableRanges(reqRanges,content_length);
//
//            //  if there are no satisfiable ranges, send 416 response
//            if (ranges==null || ranges.size()==0)
//            {
//                writeHeaders(response, content, content_length);
//                response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
//                response.setHeader(HttpHeader.CONTENT_RANGE.asString(),
//                        InclusiveByteRange.to416HeaderRangeString(content_length));
//                resource.writeTo(out,0,content_length);
//                return true;
//            }
//
//            //  if there is only a single valid range (must be satisfiable
//            //  since were here now), send that range with a 216 response
//            if ( ranges.size()== 1)
//            {
//                InclusiveByteRange singleSatisfiableRange = ranges.get(0);
//                long singleLength = singleSatisfiableRange.getSize(content_length);
//                writeHeaders(response,content,singleLength                     );
//                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
//                if (!response.containsHeader(HttpHeader.DATE.asString()))
//                    response.addDateHeader(HttpHeader.DATE.asString(),System.currentTimeMillis());
//                response.setHeader(HttpHeader.CONTENT_RANGE.asString(),
//                        singleSatisfiableRange.toHeaderRangeString(content_length));
//                resource.writeTo(out,singleSatisfiableRange.getFirst(content_length),singleLength);
//                return true;
//            }
//
//            //  multiple non-overlapping valid ranges cause a multipart
//            //  216 response which does not require an overall
//            //  content-length header
//            //
//            writeHeaders(response,content,-1);
//            String mimetype=(content==null?null:content.getContentType());
//            if (mimetype==null)
//                LOG.warn("Unknown mimetype for "+request.getRequestURI());
//            MultiPartOutputStream multi = new MultiPartOutputStream(out);
//            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
//            if (!response.containsHeader(HttpHeader.DATE.asString()))
//                response.addDateHeader(HttpHeader.DATE.asString(),System.currentTimeMillis());
//
//            // If the request has a "Request-Range" header then we need to
//            // send an old style multipart/x-byteranges Content-Type. This
//            // keeps Netscape and acrobat happy. This is what Apache does.
//            String ctp;
//            if (request.getHeader(HttpHeader.REQUEST_RANGE.asString())!=null)
//                ctp = "multipart/x-byteranges; boundary=";
//            else
//                ctp = "multipart/byteranges; boundary=";
//            response.setContentType(ctp+multi.getBoundary());
//
//            InputStream in=resource.getInputStream();
//            long pos=0;
//
//            // calculate the content-length
//            int length=0;
//            String[] header = new String[ranges.size()];
//            for (int i=0;i<ranges.size();i++)
//            {
//                InclusiveByteRange ibr = ranges.get(i);
//                header[i]=ibr.toHeaderRangeString(content_length);
//                length+=
//                        ((i>0)?2:0)+
//                                2+multi.getBoundary().length()+2+
//                                (mimetype==null?0:HttpHeader.CONTENT_TYPE.asString().length()+2+mimetype.length())+2+
//                                HttpHeader.CONTENT_RANGE.asString().length()+2+header[i].length()+2+
//                                2+
//                                (ibr.getLast(content_length)-ibr.getFirst(content_length))+1;
//            }
//            length+=2+2+multi.getBoundary().length()+2+2;
//            response.setContentLength(length);
//
//            for (int i=0;i<ranges.size();i++)
//            {
//                InclusiveByteRange ibr =  ranges.get(i);
//                multi.startPart(mimetype,new String[]{HttpHeader.CONTENT_RANGE+": "+header[i]});
//
//                long start=ibr.getFirst(content_length);
//                long size=ibr.getSize(content_length);
//                if (in!=null)
//                {
//                    // Handle non cached resource
//                    if (start<pos)
//                    {
//                        in.close();
//                        in=resource.getInputStream();
//                        pos=0;
//                    }
//                    if (pos<start)
//                    {
//                        in.skip(start-pos);
//                        pos=start;
//                    }
//
//                    IO.copy(in,multi,size);
//                    pos+=size;
//                }
//                else
//                    // Handle cached resource
//                    (resource).writeTo(multi,start,size);
//            }
//            if (in!=null)
//                in.close();
//            multi.close();
//        }
//        return true;
//    }
//
//    /* ------------------------------------------------------------ */
//    protected void writeHeaders(HttpServletResponse response,HttpContent content,long count)
//    {
//        if (content == null)
//        {
//            // No content, then no headers to process
//            // This is possible during bypass write because of wrapping
//            // See .sendData() for more details.
//            return;
//        }
//
//        if (content.getContentType()!=null && response.getContentType()==null)
//            response.setContentType(content.getContentType().toString());
//
//        if (response instanceof Response)
//        {
//            Response r=(Response)response;
//            HttpFields fields = r.getHttpFields();
//
//            if (content.getLastModified()!=null)
//                fields.put(HttpHeader.LAST_MODIFIED,content.getLastModified());
//            else if (content.getResource()!=null)
//            {
//                long lml=content.getResource().lastModified();
//                if (lml!=-1)
//                    fields.putDateField(HttpHeader.LAST_MODIFIED,lml);
//            }
//
//            if (count != -1)
//                r.setLongContentLength(count);
//
//            writeOptionHeaders(fields);
//
//            if (_etags)
//                fields.put(HttpHeader.ETAG,content.getETag());
//        }
//        else
//        {
//            long lml=content.getResource().lastModified();
//            if (lml>=0)
//                response.setDateHeader(HttpHeader.LAST_MODIFIED.asString(),lml);
//
//            if (count != -1)
//            {
//                if (count<Integer.MAX_VALUE)
//                    response.setContentLength((int)count);
//                else
//                    response.setHeader(HttpHeader.CONTENT_LENGTH.asString(),Long.toString(count));
//            }
//
//            writeOptionHeaders(response);
//
//            if (_etags)
//                response.setHeader(HttpHeader.ETAG.asString(),content.getETag());
//        }
//    }
//
//    /* ------------------------------------------------------------ */
//    protected void writeOptionHeaders(HttpFields fields)
//    {
//        if (_acceptRanges)
//            fields.put(ACCEPT_RANGES);
//
//        if (_cacheControl!=null)
//            fields.put(_cacheControl);
//    }
//
//    /* ------------------------------------------------------------ */
//    protected void writeOptionHeaders(HttpServletResponse response)
//    {
//        if (_acceptRanges)
//            response.setHeader(HttpHeader.ACCEPT_RANGES.asString(),"bytes");
//
//        if (_cacheControl!=null)
//            response.setHeader(HttpHeader.CACHE_CONTROL.asString(),_cacheControl.getValue());
//    }
//
//    /* ------------------------------------------------------------ */
//    /*
//     * @see javax.servlet.Servlet#destroy()
//     */
//    @Override
//    public void destroy()
//    {
//        if (_cache!=null)
//            _cache.flushCache();
//        super.destroy();
//    }
//
//}