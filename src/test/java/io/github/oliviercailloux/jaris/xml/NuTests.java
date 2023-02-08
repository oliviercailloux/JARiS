package io.github.oliviercailloux.jaris.xml;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thaiopensource.relaxng.impl.CombineValidator;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.ValidationDriver;
import com.thaiopensource.validate.Validator;
import com.thaiopensource.validate.auto.AutoSchemaReader;
import com.thaiopensource.validate.prop.rng.RngProperty;
import com.thaiopensource.validate.rng.CompactSchemaReader;
import com.thaiopensource.xml.sax.CountingErrorHandler;
import com.thaiopensource.xml.sax.ErrorHandlerImpl;
import com.thaiopensource.xml.sax.Jaxp11XMLReaderCreator;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import nu.validator.checker.ConformingButObsoleteWarner;
import nu.validator.checker.MicrodataChecker;
import nu.validator.checker.NormalizationChecker;
import nu.validator.checker.TextContentChecker;
import nu.validator.checker.UncheckedSubtreeWarner;
import nu.validator.checker.UnsupportedFeatureChecker;
import nu.validator.checker.UsemapChecker;
import nu.validator.checker.XmlPiChecker;
import nu.validator.checker.jing.CheckerSchema;
import nu.validator.checker.jing.CheckerValidator;
import nu.validator.checker.table.TableChecker;
import nu.validator.client.EmbeddedValidator;
import nu.validator.gnu.xml.aelfred2.SAXDriver;
import nu.validator.htmlparser.common.Heuristics;
import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;
import nu.validator.localentities.LocalCacheEntityResolver;
import nu.validator.messages.GnuMessageEmitter;
import nu.validator.messages.MessageEmitterAdapter;
import nu.validator.servlet.imagereview.ImageCollector;
import nu.validator.source.SourceCode;
import nu.validator.validation.SimpleDocumentValidator;
import nu.validator.xml.IdFilter;
import nu.validator.xml.NullEntityResolver;
import nu.validator.xml.PrudentHttpEntityResolver;
import nu.validator.xml.SystemErrErrorHandler;
import nu.validator.xml.TypedInputSource;
import nu.validator.xml.WiretapXMLReaderWrapper;
import nu.validator.xml.customelements.NamespaceChangingSchemaWrapper;
import nu.validator.xml.dataattributes.DataAttributeDroppingSchemaWrapper;
import nu.validator.xml.langattributes.XmlLangAttributeDroppingSchemaWrapper;
import nu.validator.xml.roleattributes.RoleAttributeFilteringSchemaWrapper;
import nu.validator.xml.templateelement.TemplateElementDroppingSchemaWrapper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

/**
 * https://search.maven.org/artifact/nu.validator/validator/20.7.2/jar depends on
 * nu.validator:jing:20200702VNU
 * https://search.maven.org/artifact/nu.validator/jing/20200702VNU/jar. There’s also
 * https://search.maven.org/artifact/org.relaxng/jing/20220510/jar.
 */
public class NuTests {
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(NuTests.class);

  private static final String MSG_SUCCESS = "Document checking completed. No errors found.";
  private static final String MSG_FAIL = "Document checking completed.";
  private static final String EXTENSION_ERROR =
      "File was not checked. Files must have .html, .xhtml, .htm, or .xht extensions.";

  @Test
  void testEmbeddedValidatorAccepts() throws Exception {
    final Path html = Path.of(getClass().getResource("simple.xhtml").toURI());
    final EmbeddedValidator validator = new EmbeddedValidator();
    validator.setOutputFormat(EmbeddedValidator.OutputFormat.GNU);
    final String output = validator.validate(html);
    assertTrue(output.isEmpty(), output);
  }

  @Test
  void testEmbeddedValidatorRejects() throws Exception {
    final Path html = Path.of(getClass().getResource("invalid.html").toURI());
    final EmbeddedValidator validator = new EmbeddedValidator();
    validator.setOutputFormat(EmbeddedValidator.OutputFormat.GNU);
    final String output = validator.validate(html);
    LOGGER.info("Rejected invalid: {}.", output);
    assertFalse(output.isEmpty());
  }

  @Test
  void testUnwrappedRejects() throws Exception {
    final Path path = Path.of(getClass().getResource("invalid.html").toURI());
    final boolean asciiQuotes = false;
    final boolean detectLanguages = false;
    final boolean forceHtml = false;
    final boolean loadEntities = false;
    final boolean noStream = false;
    final SimpleDocumentValidator sdvalidator =
        new SimpleDocumentValidator(true, false, !detectLanguages);
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    boolean showSource = true;
    boolean batchMode = true;
    final GnuMessageEmitter emitter = new GnuMessageEmitter(out, asciiQuotes);
    final MessageEmitterAdapter adapter =
        new MessageEmitterAdapter(null, sdvalidator.getSourceCode(), showSource,
            new ImageCollector(sdvalidator.getSourceCode()), 0, batchMode, emitter);
    adapter.setErrorsOnly(false);
    adapter.setHtml(true);
    adapter.start(null);
    final org.xml.sax.ErrorHandler errorHandler = adapter;
    try {
      sdvalidator.setUpMainSchema(EmbeddedValidator.SCHEMA_URL, new SystemErrErrorHandler());
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    sdvalidator.setUpValidatorAndParsers(errorHandler, noStream, loadEntities);

    try {
      if (Files.notExists(path) || !Files.isReadable(path)) {
        errorHandler
            .warning(new SAXParseException("File not found.", null, path.toString(), -1, -1));
      } else if (isXhtml(path.toFile())) {
        if (forceHtml) {
          sdvalidator.checkHtmlFile(path.toFile(), true);
        } else {
          sdvalidator.checkXmlFile(path.toFile());
        }
      } else if (isHtml(path.toFile())) {
        sdvalidator.checkHtmlFile(path.toFile(), true);
      } else {
        errorHandler.warning(new SAXParseException(EXTENSION_ERROR, null, path.toString(), -1, -1));
      }
    } catch (SAXException e) {
      errorHandler.warning(new SAXParseException(e.getMessage(), null, path.toString(), -1, -1));
    }

    adapter.end(MSG_SUCCESS, MSG_FAIL, "");
    final String output = new String(out.toByteArray(), StandardCharsets.UTF_8);
    LOGGER.info("Rejected invalid: {}.", output);
    assertFalse(output.isEmpty());
  }

  /**
   * Seems to correspond to what is called through SDV, apparently called from the jar (I didn’t
   * check). See https://gist.github.com/vincent-zurczak/23e0f626eaafab96cb32 .
   */
  @Test
  void testUnwrappedViaSimpleDocumentValidatorRejects() throws Exception {
    final Path path = Path.of(getClass().getResource("invalid.html").toURI());
    final boolean asciiQuotes = false;
    final boolean detectLanguages = false;
    final boolean loadExternalEnts = false;
    final boolean noStream = false;
    final LocalCacheEntityResolver entityResolver;

    boolean hasHtml5Schema = false;

    com.thaiopensource.validate.Validator validator;

    final nu.validator.source.SourceCode sourceCode = new SourceCode();

    final org.xml.sax.XMLReader htmlReader;

    final nu.validator.gnu.xml.aelfred2.SAXDriver xmlParser;

    final org.xml.sax.XMLReader xmlReader;

    boolean enableLanguageDetection = !detectLanguages;
    PrudentHttpEntityResolver.setParams(
        Integer.parseInt(System.getProperty("nu.validator.servlet.connection-timeout", "5000")),
        Integer.parseInt(System.getProperty("nu.validator.servlet.socket-timeout", "5000")),
        Integer.parseInt(System.getProperty("nu.validator.servlet.max-requests", "100")));
    System.setProperty("nu.validator.checker.enableLangDetection", "0");
    if (enableLanguageDetection) {
      System.setProperty("nu.validator.checker.enableLangDetection", "1");
    }
    entityResolver = new LocalCacheEntityResolver(new NullEntityResolver());
    entityResolver.setAllowRnc(true);
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    boolean showSource = true;
    boolean batchMode = true;
    final GnuMessageEmitter emitter = new GnuMessageEmitter(out, asciiQuotes);
    final MessageEmitterAdapter adapter = new MessageEmitterAdapter(null, sourceCode, showSource,
        new ImageCollector(sourceCode), 0, batchMode, emitter);
    adapter.setErrorsOnly(false);
    adapter.setHtml(true);
    adapter.start(null);
    String schemaUrl = EmbeddedValidator.SCHEMA_URL;
    final SystemErrErrorHandler errorHandler = new SystemErrErrorHandler();
    Schema schema;
    {
      PropertyMapBuilder pmb = new PropertyMapBuilder();
      pmb.put(ValidateProperty.ERROR_HANDLER, errorHandler);
      pmb.put(ValidateProperty.ENTITY_RESOLVER, entityResolver);
      pmb.put(ValidateProperty.XML_READER_CREATOR, new Jaxp11XMLReaderCreator());
      RngProperty.CHECK_ID_IDREF.add(pmb);
      PropertyMap jingPropertyMap = pmb.toPropertyMap();

      TypedInputSource schemaInput =
          (TypedInputSource) entityResolver.resolveEntity(null, schemaUrl);
      SchemaReader sr;
      if ("application/relax-ng-compact-syntax".equals(schemaInput.getType())) {
        sr = CompactSchemaReader.getInstance();
      } else {
        sr = new AutoSchemaReader();
      }
      schema = sr.createSchema(schemaInput, jingPropertyMap);
    }
    if (schemaUrl.contains("html5")) {
      schema = new DataAttributeDroppingSchemaWrapper(schema);
      schema = new XmlLangAttributeDroppingSchemaWrapper(schema);
      schema = new RoleAttributeFilteringSchemaWrapper(schema);
      schema = new TemplateElementDroppingSchemaWrapper(schema);
      schema = new NamespaceChangingSchemaWrapper(schema);
      hasHtml5Schema = true;
      if ("http://s.validator.nu/html5-all.rnc".equals(schemaUrl)) {
        System.setProperty("nu.validator.schema.rdfa-full", "1");
      } else {
        System.setProperty("nu.validator.schema.rdfa-full", "0");
      }
    }

    final com.thaiopensource.validate.Schema mainSchema = schema;

    final org.xml.sax.ErrorHandler errorHandlerA = adapter;
    PropertyMap jingPropertyMap;
    {
      PropertyMapBuilder pmb = new PropertyMapBuilder();
      pmb.put(ValidateProperty.ERROR_HANDLER, errorHandlerA);
      pmb.put(ValidateProperty.XML_READER_CREATOR, new Jaxp11XMLReaderCreator());
      RngProperty.CHECK_ID_IDREF.add(pmb);
      jingPropertyMap = pmb.toPropertyMap();
    }

    validator = mainSchema.createValidator(jingPropertyMap);

    if (hasHtml5Schema) {
      Validator assertionValidator = CheckerSchema.ASSERTION_SCH.createValidator(jingPropertyMap);
      validator = new CombineValidator(validator, assertionValidator);
      Validator langdetectValidator =
          CheckerSchema.LANGUAGE_DETECTING_CHECKER.createValidator(jingPropertyMap);
      validator = new CombineValidator(validator, langdetectValidator);
      validator = new CombineValidator(validator,
          new CheckerValidator(new TableChecker(), jingPropertyMap));
      validator = new CombineValidator(validator,
          new CheckerValidator(new ConformingButObsoleteWarner(), jingPropertyMap));
      validator = new CombineValidator(validator,
          new CheckerValidator(new MicrodataChecker(), jingPropertyMap));
      validator = new CombineValidator(validator,
          new CheckerValidator(new NormalizationChecker(), jingPropertyMap));
      validator = new CombineValidator(validator,
          new CheckerValidator(new TextContentChecker(), jingPropertyMap));
      validator = new CombineValidator(validator,
          new CheckerValidator(new UncheckedSubtreeWarner(), jingPropertyMap));
      validator = new CombineValidator(validator,
          new CheckerValidator(new UnsupportedFeatureChecker(), jingPropertyMap));
      validator = new CombineValidator(validator,
          new CheckerValidator(new UsemapChecker(), jingPropertyMap));
      validator = new CombineValidator(validator,
          new CheckerValidator(new XmlPiChecker(), jingPropertyMap));
    }

    HtmlParser htmlParser = new HtmlParser();
    htmlParser.addCharacterHandler(sourceCode);
    htmlParser.setCommentPolicy(XmlViolationPolicy.ALLOW);
    htmlParser.setContentNonXmlCharPolicy(XmlViolationPolicy.ALLOW);
    htmlParser.setContentSpacePolicy(XmlViolationPolicy.ALTER_INFOSET);
    htmlParser.setNamePolicy(XmlViolationPolicy.ALLOW);
    htmlParser.setXmlnsPolicy(XmlViolationPolicy.ALTER_INFOSET);
    htmlParser.setMappingLangToXmlLang(true);
    htmlParser.setHeuristics(Heuristics.ALL);
    htmlParser.setContentHandler(validator.getContentHandler());
    htmlParser.setErrorHandler(errorHandlerA);
    htmlParser.setNamePolicy(XmlViolationPolicy.ALLOW);
    htmlParser.setMappingLangToXmlLang(true);
    htmlParser.setFeature("http://xml.org/sax/features/unicode-normalization-checking", true);
    if (!noStream) {
      htmlParser.setStreamabilityViolationPolicy(XmlViolationPolicy.FATAL);
    }
    htmlReader = getWiretap(htmlParser, sourceCode.getLocationRecorder());
    xmlParser = new SAXDriver();
    xmlParser.setContentHandler(validator.getContentHandler());
    final IdFilter xmlReader1 = new IdFilter(xmlParser);
    xmlReader1.setFeature("http://xml.org/sax/features/string-interning", true);
    xmlReader1.setContentHandler(validator.getContentHandler());
    xmlReader1.setFeature("http://xml.org/sax/features/unicode-normalization-checking", true);
    if (loadExternalEnts) {
      xmlReader1.setEntityResolver(entityResolver);
    } else {
      xmlReader1.setFeature("http://xml.org/sax/features/external-general-entities", false);
      xmlReader1.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      xmlReader1.setEntityResolver(new NullEntityResolver());
    }
    xmlReader = getWiretap(xmlParser, sourceCode.getLocationRecorder());
    xmlParser.setErrorHandler(errorHandlerA);
    xmlParser.lockErrorHandler();

    /* This is for non-XML documents. */
    {
      validator.reset();
      final InputSource is = new InputSource(new StringReader(Files.readString(path)));
      is.setEncoding("UTF-8");
      sourceCode.initialize(is);
      htmlReader.parse(is);
    }

    /* This is for XML documents. */
    {
      validator.reset();
      final InputSource is = new InputSource(new StringReader(Files.readString(path)));
      xmlParser.setCharacterHandler(sourceCode);
      sourceCode.initialize(is);
      xmlReader.parse(is);
    }

    adapter.end(MSG_SUCCESS, MSG_FAIL, "");
    final String output = new String(out.toByteArray(), StandardCharsets.UTF_8);
    LOGGER.info("Rejected invalid: {}.", output);
    assertFalse(output.isEmpty());
  }

  @Test
  void testUnwrappedXhtmlRejects() throws Exception {
    final Path path = Path.of(getClass().getResource("simple invalid.html").toURI());
    final boolean asciiQuotes = false;
    final boolean detectLanguages = false;
    final boolean loadExternalEnts = false;
    final LocalCacheEntityResolver entityResolver;

    boolean hasHtml5Schema = false;

    com.thaiopensource.validate.Validator validator;

    final nu.validator.source.SourceCode sourceCode = new SourceCode();

    final nu.validator.gnu.xml.aelfred2.SAXDriver xmlParser;

    final org.xml.sax.XMLReader xmlReader;

    boolean enableLanguageDetection = !detectLanguages;
    PrudentHttpEntityResolver.setParams(
        Integer.parseInt(System.getProperty("nu.validator.servlet.connection-timeout", "5000")),
        Integer.parseInt(System.getProperty("nu.validator.servlet.socket-timeout", "5000")),
        Integer.parseInt(System.getProperty("nu.validator.servlet.max-requests", "100")));
    System.setProperty("nu.validator.checker.enableLangDetection", "0");
    if (enableLanguageDetection) {
      System.setProperty("nu.validator.checker.enableLangDetection", "1");
    }
    entityResolver = new LocalCacheEntityResolver(new NullEntityResolver());
    entityResolver.setAllowRnc(true);
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    boolean showSource = true;
    boolean batchMode = true;
    final GnuMessageEmitter emitter = new GnuMessageEmitter(out, asciiQuotes);
    final MessageEmitterAdapter adapter = new MessageEmitterAdapter(null, sourceCode, showSource,
        new ImageCollector(sourceCode), 0, batchMode, emitter);
    adapter.setErrorsOnly(false);
    adapter.setHtml(true);
    adapter.start(null);
    String schemaUrl = EmbeddedValidator.SCHEMA_URL;
    final SystemErrErrorHandler errorHandler = new SystemErrErrorHandler();
    com.thaiopensource.validate.Schema schema;
    {
      PropertyMapBuilder pmb = new PropertyMapBuilder();
      pmb.put(ValidateProperty.ERROR_HANDLER, errorHandler);
      pmb.put(ValidateProperty.ENTITY_RESOLVER, entityResolver);
      pmb.put(ValidateProperty.XML_READER_CREATOR, new Jaxp11XMLReaderCreator());
      RngProperty.CHECK_ID_IDREF.add(pmb);
      PropertyMap jingPropertyMap = pmb.toPropertyMap();

      TypedInputSource schemaInput =
          (TypedInputSource) entityResolver.resolveEntity(null, schemaUrl);
      SchemaReader sr;
      if ("application/relax-ng-compact-syntax".equals(schemaInput.getType())) {
        sr = CompactSchemaReader.getInstance();
      } else {
        sr = new AutoSchemaReader();
      }
      schema = sr.createSchema(schemaInput, jingPropertyMap);
    }
    if (schemaUrl.contains("html5")) {
      schema = new DataAttributeDroppingSchemaWrapper(schema);
      schema = new XmlLangAttributeDroppingSchemaWrapper(schema);
      schema = new RoleAttributeFilteringSchemaWrapper(schema);
      schema = new TemplateElementDroppingSchemaWrapper(schema);
      schema = new NamespaceChangingSchemaWrapper(schema);
      hasHtml5Schema = true;
      if ("http://s.validator.nu/html5-all.rnc".equals(schemaUrl)) {
        System.setProperty("nu.validator.schema.rdfa-full", "1");
      } else {
        System.setProperty("nu.validator.schema.rdfa-full", "0");
      }
    }
    final org.xml.sax.ErrorHandler errorHandlerA = adapter;
    PropertyMap jingPropertyMap;
    {
      PropertyMapBuilder pmb = new PropertyMapBuilder();
      pmb.put(ValidateProperty.ERROR_HANDLER, errorHandlerA);
      pmb.put(ValidateProperty.XML_READER_CREATOR, new Jaxp11XMLReaderCreator());
      RngProperty.CHECK_ID_IDREF.add(pmb);
      jingPropertyMap = pmb.toPropertyMap();
    }

    validator = schema.createValidator(jingPropertyMap);

    if (hasHtml5Schema) {
      Validator assertionValidator = CheckerSchema.ASSERTION_SCH.createValidator(jingPropertyMap);
      validator = new CombineValidator(validator, assertionValidator);
      Validator langdetectValidator =
          CheckerSchema.LANGUAGE_DETECTING_CHECKER.createValidator(jingPropertyMap);
      validator = new CombineValidator(validator, langdetectValidator);
      validator = new CombineValidator(validator,
          new CheckerValidator(new TableChecker(), jingPropertyMap));
      validator = new CombineValidator(validator,
          new CheckerValidator(new ConformingButObsoleteWarner(), jingPropertyMap));
      validator = new CombineValidator(validator,
          new CheckerValidator(new MicrodataChecker(), jingPropertyMap));
      validator = new CombineValidator(validator,
          new CheckerValidator(new NormalizationChecker(), jingPropertyMap));
      validator = new CombineValidator(validator,
          new CheckerValidator(new TextContentChecker(), jingPropertyMap));
      validator = new CombineValidator(validator,
          new CheckerValidator(new UncheckedSubtreeWarner(), jingPropertyMap));
      validator = new CombineValidator(validator,
          new CheckerValidator(new UnsupportedFeatureChecker(), jingPropertyMap));
      validator = new CombineValidator(validator,
          new CheckerValidator(new UsemapChecker(), jingPropertyMap));
      validator = new CombineValidator(validator,
          new CheckerValidator(new XmlPiChecker(), jingPropertyMap));
    }

    xmlParser = new SAXDriver();
    xmlParser.setContentHandler(validator.getContentHandler());
    final IdFilter xmlReader1 = new IdFilter(xmlParser);
    xmlReader1.setFeature("http://xml.org/sax/features/string-interning", true);
    xmlReader1.setContentHandler(validator.getContentHandler());
    xmlReader1.setFeature("http://xml.org/sax/features/unicode-normalization-checking", true);
    if (loadExternalEnts) {
      xmlReader1.setEntityResolver(entityResolver);
    } else {
      xmlReader1.setFeature("http://xml.org/sax/features/external-general-entities", false);
      xmlReader1.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      xmlReader1.setEntityResolver(new NullEntityResolver());
    }
    xmlReader = getWiretap(xmlParser, sourceCode.getLocationRecorder());
    xmlParser.setErrorHandler(errorHandlerA);
    xmlParser.lockErrorHandler();

    {
      validator.reset();
      final InputSource is = new InputSource(new StringReader(Files.readString(path)));
      xmlParser.setCharacterHandler(sourceCode);
      sourceCode.initialize(is);
      xmlReader.parse(is);
    }

    adapter.end(MSG_SUCCESS, MSG_FAIL, "");
    final String output = new String(out.toByteArray(), StandardCharsets.UTF_8);
    LOGGER.info("Rejected invalid: {}.", output);
    assertFalse(output.isEmpty());
  }

  @Test
  void testUnwrappedXhtmlRejectsSimpler() throws Exception {
    final Path path = Path.of(getClass().getResource("simple invalid.html").toURI());
    final boolean detectLanguages = false;
    final boolean loadExternalEnts = false;
    final LocalCacheEntityResolver entityResolver;

    com.thaiopensource.validate.Validator validator;

    final nu.validator.source.SourceCode sourceCode = new SourceCode();

    final nu.validator.gnu.xml.aelfred2.SAXDriver xmlParser;

    final org.xml.sax.XMLReader xmlReader;

    boolean enableLanguageDetection = !detectLanguages;
    PrudentHttpEntityResolver.setParams(
        Integer.parseInt(System.getProperty("nu.validator.servlet.connection-timeout", "5000")),
        Integer.parseInt(System.getProperty("nu.validator.servlet.socket-timeout", "5000")),
        Integer.parseInt(System.getProperty("nu.validator.servlet.max-requests", "100")));
    System.setProperty("nu.validator.checker.enableLangDetection", "0");
    if (enableLanguageDetection) {
      System.setProperty("nu.validator.checker.enableLangDetection", "1");
    }
    entityResolver = new LocalCacheEntityResolver(new NullEntityResolver());
    entityResolver.setAllowRnc(true);
    String schemaUrl = EmbeddedValidator.SCHEMA_URL;
    com.thaiopensource.validate.Schema schema;
    {
      final SystemErrErrorHandler errorHandler = new SystemErrErrorHandler();
      PropertyMapBuilder pmb = new PropertyMapBuilder();
      pmb.put(ValidateProperty.ERROR_HANDLER, errorHandler);
      pmb.put(ValidateProperty.ENTITY_RESOLVER, entityResolver);
      pmb.put(ValidateProperty.XML_READER_CREATOR, new Jaxp11XMLReaderCreator());
      RngProperty.CHECK_ID_IDREF.add(pmb);
      PropertyMap jingPropertyMap = pmb.toPropertyMap();

      TypedInputSource schemaInput =
          (TypedInputSource) entityResolver.resolveEntity(null, schemaUrl);
      SchemaReader sr;
      if ("application/relax-ng-compact-syntax".equals(schemaInput.getType())) {
        sr = CompactSchemaReader.getInstance();
      } else {
        sr = new AutoSchemaReader();
      }
      schema = sr.createSchema(schemaInput, jingPropertyMap);
    }
    if (schemaUrl.contains("html5")) {
      schema = new DataAttributeDroppingSchemaWrapper(schema);
      schema = new XmlLangAttributeDroppingSchemaWrapper(schema);
      schema = new RoleAttributeFilteringSchemaWrapper(schema);
      schema = new TemplateElementDroppingSchemaWrapper(schema);
      schema = new NamespaceChangingSchemaWrapper(schema);
      if ("http://s.validator.nu/html5-all.rnc".equals(schemaUrl)) {
        System.setProperty("nu.validator.schema.rdfa-full", "1");
      } else {
        System.setProperty("nu.validator.schema.rdfa-full", "0");
      }
    }
    final CountingErrorHandler errorHandlerA =
        new CountingErrorHandler(new SystemErrErrorHandler());
    PropertyMap jingPropertyMap;
    {
      PropertyMapBuilder pmb = new PropertyMapBuilder();
      pmb.put(ValidateProperty.ERROR_HANDLER, errorHandlerA);
      pmb.put(ValidateProperty.XML_READER_CREATOR, new Jaxp11XMLReaderCreator());
      RngProperty.CHECK_ID_IDREF.add(pmb);
      jingPropertyMap = pmb.toPropertyMap();
    }

    validator = schema.createValidator(jingPropertyMap);

    Validator assertionValidator = CheckerSchema.ASSERTION_SCH.createValidator(jingPropertyMap);
    validator = new CombineValidator(validator, assertionValidator);
    Validator langdetectValidator =
        CheckerSchema.LANGUAGE_DETECTING_CHECKER.createValidator(jingPropertyMap);
    validator = new CombineValidator(validator, langdetectValidator);
    validator =
        new CombineValidator(validator, new CheckerValidator(new TableChecker(), jingPropertyMap));
    validator = new CombineValidator(validator,
        new CheckerValidator(new ConformingButObsoleteWarner(), jingPropertyMap));
    validator = new CombineValidator(validator,
        new CheckerValidator(new MicrodataChecker(), jingPropertyMap));
    validator = new CombineValidator(validator,
        new CheckerValidator(new NormalizationChecker(), jingPropertyMap));
    validator = new CombineValidator(validator,
        new CheckerValidator(new TextContentChecker(), jingPropertyMap));
    validator = new CombineValidator(validator,
        new CheckerValidator(new UncheckedSubtreeWarner(), jingPropertyMap));
    validator = new CombineValidator(validator,
        new CheckerValidator(new UnsupportedFeatureChecker(), jingPropertyMap));
    validator =
        new CombineValidator(validator, new CheckerValidator(new UsemapChecker(), jingPropertyMap));
    validator =
        new CombineValidator(validator, new CheckerValidator(new XmlPiChecker(), jingPropertyMap));

    xmlParser = new SAXDriver();
    xmlParser.setContentHandler(validator.getContentHandler());
    final IdFilter xmlReader1 = new IdFilter(xmlParser);
    xmlReader1.setFeature("http://xml.org/sax/features/string-interning", true);
    xmlReader1.setContentHandler(validator.getContentHandler());
    xmlReader1.setFeature("http://xml.org/sax/features/unicode-normalization-checking", true);
    if (loadExternalEnts) {
      xmlReader1.setEntityResolver(entityResolver);
    } else {
      xmlReader1.setFeature("http://xml.org/sax/features/external-general-entities", false);
      xmlReader1.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      xmlReader1.setEntityResolver(new NullEntityResolver());
    }
    xmlReader = getWiretap(xmlParser, sourceCode.getLocationRecorder());
    xmlParser.setErrorHandler(errorHandlerA);
    xmlParser.lockErrorHandler();

    {
      validator.reset();
      final InputSource is = new InputSource(new StringReader(Files.readString(path)));
      xmlParser.setCharacterHandler(sourceCode);
      sourceCode.initialize(is);
      xmlReader.parse(is);
    }

    assertTrue(errorHandlerA.getHadErrorOrFatalError());
  }

  /**
   * https://stackoverflow.com/a/44038951
   *
   * java -cp vnu.jar com.thaiopensource.relaxng.util.Driver \ -c
   * https://raw.github.com/validator/validator/master/schema/html5/xhtml5.rnc \ FILE.xhtml
   */
  @Test
  void testViaJingCmdRejects() throws Exception {
    ErrorHandlerImpl eh = new ErrorHandlerImpl(System.out);
    PropertyMapBuilder properties = new PropertyMapBuilder();
    ValidateProperty.ERROR_HANDLER.put(properties, eh);
    RngProperty.CHECK_ID_IDREF.add(properties);
    SchemaReader sr = CompactSchemaReader.getInstance();
    ValidationDriver driver = new ValidationDriver(properties.toPropertyMap(), sr);
    InputSource in = ValidationDriver.uriOrFileInputSource(
        "https://raw.github.com/validator/validator/master/schema/html5/xhtml5.rnc");
    final boolean loaded = driver.loadSchema(in);
    assertTrue(loaded);
    final boolean validated = driver.validate(ValidationDriver.uriOrFileInputSource(
        "src/test/resources/io/github/oliviercailloux/jaris/xml/simple%20invalid.html"));
    assertFalse(validated);
  }

  private static WiretapXMLReaderWrapper getWiretap(XMLReader reader,
      ContentHandler locationRecorder) {
    WiretapXMLReaderWrapper wiretap = new WiretapXMLReaderWrapper(reader);
    wiretap.setWiretapContentHander(locationRecorder);
    wiretap.setWiretapLexicalHandler((LexicalHandler) locationRecorder);
    return wiretap;
  }

  private static boolean isXhtml(File file) {
    String name = file.getName();
    return name.endsWith(".xhtml") || name.endsWith(".xht");
  }

  private static boolean isHtml(File file) {
    String name = file.getName();
    return name.endsWith(".html") || name.endsWith(".htm");
  }
}
