var __defProp = Object.defineProperty;
var __name = (target, value) => __defProp(target, "name", { value, configurable: true });

// ../tools/tools_npm/node_modules/wrangler/node_modules/unenv/dist/runtime/_internal/utils.mjs
// @__NO_SIDE_EFFECTS__
function createNotImplementedError(name) {
  return new Error(`[unenv] ${name} is not implemented yet!`);
}
__name(createNotImplementedError, "createNotImplementedError");
// @__NO_SIDE_EFFECTS__
function notImplemented(name) {
  const fn = /* @__PURE__ */ __name(() => {
    throw /* @__PURE__ */ createNotImplementedError(name);
  }, "fn");
  return Object.assign(fn, { __unenv__: true });
}
__name(notImplemented, "notImplemented");
// @__NO_SIDE_EFFECTS__
function notImplementedClass(name) {
  return class {
    __unenv__ = true;
    constructor() {
      throw new Error(`[unenv] ${name} is not implemented yet!`);
    }
  };
}
__name(notImplementedClass, "notImplementedClass");

// ../tools/tools_npm/node_modules/wrangler/node_modules/unenv/dist/runtime/node/internal/perf_hooks/performance.mjs
var _timeOrigin = globalThis.performance?.timeOrigin ?? Date.now();
var _performanceNow = globalThis.performance?.now ? globalThis.performance.now.bind(globalThis.performance) : () => Date.now() - _timeOrigin;
var nodeTiming = {
  name: "node",
  entryType: "node",
  startTime: 0,
  duration: 0,
  nodeStart: 0,
  v8Start: 0,
  bootstrapComplete: 0,
  environment: 0,
  loopStart: 0,
  loopExit: 0,
  idleTime: 0,
  uvMetricsInfo: {
    loopCount: 0,
    events: 0,
    eventsWaiting: 0
  },
  detail: void 0,
  toJSON() {
    return this;
  }
};
var PerformanceEntry = class {
  static {
    __name(this, "PerformanceEntry");
  }
  __unenv__ = true;
  detail;
  entryType = "event";
  name;
  startTime;
  constructor(name, options) {
    this.name = name;
    this.startTime = options?.startTime || _performanceNow();
    this.detail = options?.detail;
  }
  get duration() {
    return _performanceNow() - this.startTime;
  }
  toJSON() {
    return {
      name: this.name,
      entryType: this.entryType,
      startTime: this.startTime,
      duration: this.duration,
      detail: this.detail
    };
  }
};
var PerformanceMark = class PerformanceMark2 extends PerformanceEntry {
  static {
    __name(this, "PerformanceMark");
  }
  entryType = "mark";
  constructor() {
    super(...arguments);
  }
  get duration() {
    return 0;
  }
};
var PerformanceMeasure = class extends PerformanceEntry {
  static {
    __name(this, "PerformanceMeasure");
  }
  entryType = "measure";
};
var PerformanceResourceTiming = class extends PerformanceEntry {
  static {
    __name(this, "PerformanceResourceTiming");
  }
  entryType = "resource";
  serverTiming = [];
  connectEnd = 0;
  connectStart = 0;
  decodedBodySize = 0;
  domainLookupEnd = 0;
  domainLookupStart = 0;
  encodedBodySize = 0;
  fetchStart = 0;
  initiatorType = "";
  name = "";
  nextHopProtocol = "";
  redirectEnd = 0;
  redirectStart = 0;
  requestStart = 0;
  responseEnd = 0;
  responseStart = 0;
  secureConnectionStart = 0;
  startTime = 0;
  transferSize = 0;
  workerStart = 0;
  responseStatus = 0;
};
var PerformanceObserverEntryList = class {
  static {
    __name(this, "PerformanceObserverEntryList");
  }
  __unenv__ = true;
  getEntries() {
    return [];
  }
  getEntriesByName(_name, _type) {
    return [];
  }
  getEntriesByType(type) {
    return [];
  }
};
var Performance = class {
  static {
    __name(this, "Performance");
  }
  __unenv__ = true;
  timeOrigin = _timeOrigin;
  eventCounts = /* @__PURE__ */ new Map();
  _entries = [];
  _resourceTimingBufferSize = 0;
  navigation = void 0;
  timing = void 0;
  timerify(_fn, _options) {
    throw createNotImplementedError("Performance.timerify");
  }
  get nodeTiming() {
    return nodeTiming;
  }
  eventLoopUtilization() {
    return {};
  }
  markResourceTiming() {
    return new PerformanceResourceTiming("");
  }
  onresourcetimingbufferfull = null;
  now() {
    if (this.timeOrigin === _timeOrigin) {
      return _performanceNow();
    }
    return Date.now() - this.timeOrigin;
  }
  clearMarks(markName) {
    this._entries = markName ? this._entries.filter((e) => e.name !== markName) : this._entries.filter((e) => e.entryType !== "mark");
  }
  clearMeasures(measureName) {
    this._entries = measureName ? this._entries.filter((e) => e.name !== measureName) : this._entries.filter((e) => e.entryType !== "measure");
  }
  clearResourceTimings() {
    this._entries = this._entries.filter((e) => e.entryType !== "resource" || e.entryType !== "navigation");
  }
  getEntries() {
    return this._entries;
  }
  getEntriesByName(name, type) {
    return this._entries.filter((e) => e.name === name && (!type || e.entryType === type));
  }
  getEntriesByType(type) {
    return this._entries.filter((e) => e.entryType === type);
  }
  mark(name, options) {
    const entry = new PerformanceMark(name, options);
    this._entries.push(entry);
    return entry;
  }
  measure(measureName, startOrMeasureOptions, endMark) {
    let start;
    let end;
    if (typeof startOrMeasureOptions === "string") {
      start = this.getEntriesByName(startOrMeasureOptions, "mark")[0]?.startTime;
      end = this.getEntriesByName(endMark, "mark")[0]?.startTime;
    } else {
      start = Number.parseFloat(startOrMeasureOptions?.start) || this.now();
      end = Number.parseFloat(startOrMeasureOptions?.end) || this.now();
    }
    const entry = new PerformanceMeasure(measureName, {
      startTime: start,
      detail: {
        start,
        end
      }
    });
    this._entries.push(entry);
    return entry;
  }
  setResourceTimingBufferSize(maxSize) {
    this._resourceTimingBufferSize = maxSize;
  }
  addEventListener(type, listener, options) {
    throw createNotImplementedError("Performance.addEventListener");
  }
  removeEventListener(type, listener, options) {
    throw createNotImplementedError("Performance.removeEventListener");
  }
  dispatchEvent(event) {
    throw createNotImplementedError("Performance.dispatchEvent");
  }
  toJSON() {
    return this;
  }
};
var PerformanceObserver = class {
  static {
    __name(this, "PerformanceObserver");
  }
  __unenv__ = true;
  static supportedEntryTypes = [];
  _callback = null;
  constructor(callback) {
    this._callback = callback;
  }
  takeRecords() {
    return [];
  }
  disconnect() {
    throw createNotImplementedError("PerformanceObserver.disconnect");
  }
  observe(options) {
    throw createNotImplementedError("PerformanceObserver.observe");
  }
  bind(fn) {
    return fn;
  }
  runInAsyncScope(fn, thisArg, ...args) {
    return fn.call(thisArg, ...args);
  }
  asyncId() {
    return 0;
  }
  triggerAsyncId() {
    return 0;
  }
  emitDestroy() {
    return this;
  }
};
var performance = globalThis.performance && "addEventListener" in globalThis.performance ? globalThis.performance : new Performance();

// ../tools/tools_npm/node_modules/wrangler/node_modules/@cloudflare/unenv-preset/dist/runtime/polyfill/performance.mjs
if (!("__unenv__" in performance)) {
  const proto = Performance.prototype;
  for (const key of Object.getOwnPropertyNames(proto)) {
    if (key !== "constructor" && !(key in performance)) {
      const desc = Object.getOwnPropertyDescriptor(proto, key);
      if (desc) {
        Object.defineProperty(performance, key, desc);
      }
    }
  }
}
globalThis.performance = performance;
globalThis.Performance = Performance;
globalThis.PerformanceEntry = PerformanceEntry;
globalThis.PerformanceMark = PerformanceMark;
globalThis.PerformanceMeasure = PerformanceMeasure;
globalThis.PerformanceObserver = PerformanceObserver;
globalThis.PerformanceObserverEntryList = PerformanceObserverEntryList;
globalThis.PerformanceResourceTiming = PerformanceResourceTiming;

// ../tools/tools_npm/node_modules/wrangler/node_modules/unenv/dist/runtime/node/console.mjs
import { Writable } from "node:stream";

// ../tools/tools_npm/node_modules/wrangler/node_modules/unenv/dist/runtime/mock/noop.mjs
var noop_default = Object.assign(() => {
}, { __unenv__: true });

// ../tools/tools_npm/node_modules/wrangler/node_modules/unenv/dist/runtime/node/console.mjs
var _console = globalThis.console;
var _ignoreErrors = true;
var _stderr = new Writable();
var _stdout = new Writable();
var log = _console?.log ?? noop_default;
var info = _console?.info ?? log;
var trace = _console?.trace ?? info;
var debug = _console?.debug ?? log;
var table = _console?.table ?? log;
var error = _console?.error ?? log;
var warn = _console?.warn ?? error;
var createTask = _console?.createTask ?? /* @__PURE__ */ notImplemented("console.createTask");
var clear = _console?.clear ?? noop_default;
var count = _console?.count ?? noop_default;
var countReset = _console?.countReset ?? noop_default;
var dir = _console?.dir ?? noop_default;
var dirxml = _console?.dirxml ?? noop_default;
var group = _console?.group ?? noop_default;
var groupEnd = _console?.groupEnd ?? noop_default;
var groupCollapsed = _console?.groupCollapsed ?? noop_default;
var profile = _console?.profile ?? noop_default;
var profileEnd = _console?.profileEnd ?? noop_default;
var time = _console?.time ?? noop_default;
var timeEnd = _console?.timeEnd ?? noop_default;
var timeLog = _console?.timeLog ?? noop_default;
var timeStamp = _console?.timeStamp ?? noop_default;
var Console = _console?.Console ?? /* @__PURE__ */ notImplementedClass("console.Console");
var _times = /* @__PURE__ */ new Map();
var _stdoutErrorHandler = noop_default;
var _stderrErrorHandler = noop_default;

// ../tools/tools_npm/node_modules/wrangler/node_modules/@cloudflare/unenv-preset/dist/runtime/node/console.mjs
var workerdConsole = globalThis["console"];
var {
  assert,
  clear: clear2,
  // @ts-expect-error undocumented public API
  context,
  count: count2,
  countReset: countReset2,
  // @ts-expect-error undocumented public API
  createTask: createTask2,
  debug: debug2,
  dir: dir2,
  dirxml: dirxml2,
  error: error2,
  group: group2,
  groupCollapsed: groupCollapsed2,
  groupEnd: groupEnd2,
  info: info2,
  log: log2,
  profile: profile2,
  profileEnd: profileEnd2,
  table: table2,
  time: time2,
  timeEnd: timeEnd2,
  timeLog: timeLog2,
  timeStamp: timeStamp2,
  trace: trace2,
  warn: warn2
} = workerdConsole;
Object.assign(workerdConsole, {
  Console,
  _ignoreErrors,
  _stderr,
  _stderrErrorHandler,
  _stdout,
  _stdoutErrorHandler,
  _times
});
var console_default = workerdConsole;

// ../tools/tools_npm/node_modules/wrangler/_virtual_unenv_global_polyfill-@cloudflare-unenv-preset-node-console
globalThis.console = console_default;

// ../tools/tools_npm/node_modules/wrangler/node_modules/unenv/dist/runtime/node/internal/process/hrtime.mjs
var hrtime = /* @__PURE__ */ Object.assign(/* @__PURE__ */ __name(function hrtime2(startTime) {
  const now = Date.now();
  const seconds = Math.trunc(now / 1e3);
  const nanos = now % 1e3 * 1e6;
  if (startTime) {
    let diffSeconds = seconds - startTime[0];
    let diffNanos = nanos - startTime[0];
    if (diffNanos < 0) {
      diffSeconds = diffSeconds - 1;
      diffNanos = 1e9 + diffNanos;
    }
    return [diffSeconds, diffNanos];
  }
  return [seconds, nanos];
}, "hrtime"), { bigint: /* @__PURE__ */ __name(function bigint() {
  return BigInt(Date.now() * 1e6);
}, "bigint") });

// ../tools/tools_npm/node_modules/wrangler/node_modules/unenv/dist/runtime/node/internal/process/process.mjs
import { EventEmitter } from "node:events";

// ../tools/tools_npm/node_modules/wrangler/node_modules/unenv/dist/runtime/node/internal/tty/read-stream.mjs
var ReadStream = class {
  static {
    __name(this, "ReadStream");
  }
  fd;
  isRaw = false;
  isTTY = false;
  constructor(fd) {
    this.fd = fd;
  }
  setRawMode(mode) {
    this.isRaw = mode;
    return this;
  }
};

// ../tools/tools_npm/node_modules/wrangler/node_modules/unenv/dist/runtime/node/internal/tty/write-stream.mjs
var WriteStream = class {
  static {
    __name(this, "WriteStream");
  }
  fd;
  columns = 80;
  rows = 24;
  isTTY = false;
  constructor(fd) {
    this.fd = fd;
  }
  clearLine(dir3, callback) {
    callback && callback();
    return false;
  }
  clearScreenDown(callback) {
    callback && callback();
    return false;
  }
  cursorTo(x, y, callback) {
    callback && typeof callback === "function" && callback();
    return false;
  }
  moveCursor(dx, dy, callback) {
    callback && callback();
    return false;
  }
  getColorDepth(env2) {
    return 1;
  }
  hasColors(count3, env2) {
    return false;
  }
  getWindowSize() {
    return [this.columns, this.rows];
  }
  write(str, encoding, cb) {
    if (str instanceof Uint8Array) {
      str = new TextDecoder().decode(str);
    }
    try {
      console.log(str);
    } catch {
    }
    cb && typeof cb === "function" && cb();
    return false;
  }
};

// ../tools/tools_npm/node_modules/wrangler/node_modules/unenv/dist/runtime/node/internal/process/node-version.mjs
var NODE_VERSION = "22.14.0";

// ../tools/tools_npm/node_modules/wrangler/node_modules/unenv/dist/runtime/node/internal/process/process.mjs
var Process = class _Process extends EventEmitter {
  static {
    __name(this, "Process");
  }
  env;
  hrtime;
  nextTick;
  constructor(impl) {
    super();
    this.env = impl.env;
    this.hrtime = impl.hrtime;
    this.nextTick = impl.nextTick;
    for (const prop of [...Object.getOwnPropertyNames(_Process.prototype), ...Object.getOwnPropertyNames(EventEmitter.prototype)]) {
      const value = this[prop];
      if (typeof value === "function") {
        this[prop] = value.bind(this);
      }
    }
  }
  // --- event emitter ---
  emitWarning(warning, type, code) {
    console.warn(`${code ? `[${code}] ` : ""}${type ? `${type}: ` : ""}${warning}`);
  }
  emit(...args) {
    return super.emit(...args);
  }
  listeners(eventName) {
    return super.listeners(eventName);
  }
  // --- stdio (lazy initializers) ---
  #stdin;
  #stdout;
  #stderr;
  get stdin() {
    return this.#stdin ??= new ReadStream(0);
  }
  get stdout() {
    return this.#stdout ??= new WriteStream(1);
  }
  get stderr() {
    return this.#stderr ??= new WriteStream(2);
  }
  // --- cwd ---
  #cwd = "/";
  chdir(cwd2) {
    this.#cwd = cwd2;
  }
  cwd() {
    return this.#cwd;
  }
  // --- dummy props and getters ---
  arch = "";
  platform = "";
  argv = [];
  argv0 = "";
  execArgv = [];
  execPath = "";
  title = "";
  pid = 200;
  ppid = 100;
  get version() {
    return `v${NODE_VERSION}`;
  }
  get versions() {
    return { node: NODE_VERSION };
  }
  get allowedNodeEnvironmentFlags() {
    return /* @__PURE__ */ new Set();
  }
  get sourceMapsEnabled() {
    return false;
  }
  get debugPort() {
    return 0;
  }
  get throwDeprecation() {
    return false;
  }
  get traceDeprecation() {
    return false;
  }
  get features() {
    return {};
  }
  get release() {
    return {};
  }
  get connected() {
    return false;
  }
  get config() {
    return {};
  }
  get moduleLoadList() {
    return [];
  }
  constrainedMemory() {
    return 0;
  }
  availableMemory() {
    return 0;
  }
  uptime() {
    return 0;
  }
  resourceUsage() {
    return {};
  }
  // --- noop methods ---
  ref() {
  }
  unref() {
  }
  // --- unimplemented methods ---
  umask() {
    throw createNotImplementedError("process.umask");
  }
  getBuiltinModule() {
    return void 0;
  }
  getActiveResourcesInfo() {
    throw createNotImplementedError("process.getActiveResourcesInfo");
  }
  exit() {
    throw createNotImplementedError("process.exit");
  }
  reallyExit() {
    throw createNotImplementedError("process.reallyExit");
  }
  kill() {
    throw createNotImplementedError("process.kill");
  }
  abort() {
    throw createNotImplementedError("process.abort");
  }
  dlopen() {
    throw createNotImplementedError("process.dlopen");
  }
  setSourceMapsEnabled() {
    throw createNotImplementedError("process.setSourceMapsEnabled");
  }
  loadEnvFile() {
    throw createNotImplementedError("process.loadEnvFile");
  }
  disconnect() {
    throw createNotImplementedError("process.disconnect");
  }
  cpuUsage() {
    throw createNotImplementedError("process.cpuUsage");
  }
  setUncaughtExceptionCaptureCallback() {
    throw createNotImplementedError("process.setUncaughtExceptionCaptureCallback");
  }
  hasUncaughtExceptionCaptureCallback() {
    throw createNotImplementedError("process.hasUncaughtExceptionCaptureCallback");
  }
  initgroups() {
    throw createNotImplementedError("process.initgroups");
  }
  openStdin() {
    throw createNotImplementedError("process.openStdin");
  }
  assert() {
    throw createNotImplementedError("process.assert");
  }
  binding() {
    throw createNotImplementedError("process.binding");
  }
  // --- attached interfaces ---
  permission = { has: /* @__PURE__ */ notImplemented("process.permission.has") };
  report = {
    directory: "",
    filename: "",
    signal: "SIGUSR2",
    compact: false,
    reportOnFatalError: false,
    reportOnSignal: false,
    reportOnUncaughtException: false,
    getReport: /* @__PURE__ */ notImplemented("process.report.getReport"),
    writeReport: /* @__PURE__ */ notImplemented("process.report.writeReport")
  };
  finalization = {
    register: /* @__PURE__ */ notImplemented("process.finalization.register"),
    unregister: /* @__PURE__ */ notImplemented("process.finalization.unregister"),
    registerBeforeExit: /* @__PURE__ */ notImplemented("process.finalization.registerBeforeExit")
  };
  memoryUsage = Object.assign(() => ({
    arrayBuffers: 0,
    rss: 0,
    external: 0,
    heapTotal: 0,
    heapUsed: 0
  }), { rss: /* @__PURE__ */ __name(() => 0, "rss") });
  // --- undefined props ---
  mainModule = void 0;
  domain = void 0;
  // optional
  send = void 0;
  exitCode = void 0;
  channel = void 0;
  getegid = void 0;
  geteuid = void 0;
  getgid = void 0;
  getgroups = void 0;
  getuid = void 0;
  setegid = void 0;
  seteuid = void 0;
  setgid = void 0;
  setgroups = void 0;
  setuid = void 0;
  // internals
  _events = void 0;
  _eventsCount = void 0;
  _exiting = void 0;
  _maxListeners = void 0;
  _debugEnd = void 0;
  _debugProcess = void 0;
  _fatalException = void 0;
  _getActiveHandles = void 0;
  _getActiveRequests = void 0;
  _kill = void 0;
  _preload_modules = void 0;
  _rawDebug = void 0;
  _startProfilerIdleNotifier = void 0;
  _stopProfilerIdleNotifier = void 0;
  _tickCallback = void 0;
  _disconnect = void 0;
  _handleQueue = void 0;
  _pendingMessage = void 0;
  _channel = void 0;
  _send = void 0;
  _linkedBinding = void 0;
};

// ../tools/tools_npm/node_modules/wrangler/node_modules/@cloudflare/unenv-preset/dist/runtime/node/process.mjs
var globalProcess = globalThis["process"];
var getBuiltinModule = globalProcess.getBuiltinModule;
var workerdProcess = getBuiltinModule("node:process");
var unenvProcess = new Process({
  env: globalProcess.env,
  hrtime,
  // `nextTick` is available from workerd process v1
  nextTick: workerdProcess.nextTick
});
var { exit, features, platform } = workerdProcess;
var {
  _channel,
  _debugEnd,
  _debugProcess,
  _disconnect,
  _events,
  _eventsCount,
  _exiting,
  _fatalException,
  _getActiveHandles,
  _getActiveRequests,
  _handleQueue,
  _kill,
  _linkedBinding,
  _maxListeners,
  _pendingMessage,
  _preload_modules,
  _rawDebug,
  _send,
  _startProfilerIdleNotifier,
  _stopProfilerIdleNotifier,
  _tickCallback,
  abort,
  addListener,
  allowedNodeEnvironmentFlags,
  arch,
  argv,
  argv0,
  assert: assert2,
  availableMemory,
  binding,
  channel,
  chdir,
  config,
  connected,
  constrainedMemory,
  cpuUsage,
  cwd,
  debugPort,
  disconnect,
  dlopen,
  domain,
  emit,
  emitWarning,
  env,
  eventNames,
  execArgv,
  execPath,
  exitCode,
  finalization,
  getActiveResourcesInfo,
  getegid,
  geteuid,
  getgid,
  getgroups,
  getMaxListeners,
  getuid,
  hasUncaughtExceptionCaptureCallback,
  hrtime: hrtime3,
  initgroups,
  kill,
  listenerCount,
  listeners,
  loadEnvFile,
  mainModule,
  memoryUsage,
  moduleLoadList,
  nextTick,
  off,
  on,
  once,
  openStdin,
  permission,
  pid,
  ppid,
  prependListener,
  prependOnceListener,
  rawListeners,
  reallyExit,
  ref,
  release,
  removeAllListeners,
  removeListener,
  report,
  resourceUsage,
  send,
  setegid,
  seteuid,
  setgid,
  setgroups,
  setMaxListeners,
  setSourceMapsEnabled,
  setuid,
  setUncaughtExceptionCaptureCallback,
  sourceMapsEnabled,
  stderr,
  stdin,
  stdout,
  throwDeprecation,
  title,
  traceDeprecation,
  umask,
  unref,
  uptime,
  version,
  versions
} = unenvProcess;
var _process = {
  abort,
  addListener,
  allowedNodeEnvironmentFlags,
  hasUncaughtExceptionCaptureCallback,
  setUncaughtExceptionCaptureCallback,
  loadEnvFile,
  sourceMapsEnabled,
  arch,
  argv,
  argv0,
  chdir,
  config,
  connected,
  constrainedMemory,
  availableMemory,
  cpuUsage,
  cwd,
  debugPort,
  dlopen,
  disconnect,
  emit,
  emitWarning,
  env,
  eventNames,
  execArgv,
  execPath,
  exit,
  finalization,
  features,
  getBuiltinModule,
  getActiveResourcesInfo,
  getMaxListeners,
  hrtime: hrtime3,
  kill,
  listeners,
  listenerCount,
  memoryUsage,
  nextTick,
  on,
  off,
  once,
  pid,
  platform,
  ppid,
  prependListener,
  prependOnceListener,
  rawListeners,
  release,
  removeAllListeners,
  removeListener,
  report,
  resourceUsage,
  setMaxListeners,
  setSourceMapsEnabled,
  stderr,
  stdin,
  stdout,
  title,
  throwDeprecation,
  traceDeprecation,
  umask,
  uptime,
  version,
  versions,
  // @ts-expect-error old API
  domain,
  initgroups,
  moduleLoadList,
  reallyExit,
  openStdin,
  assert: assert2,
  binding,
  send,
  exitCode,
  channel,
  getegid,
  geteuid,
  getgid,
  getgroups,
  getuid,
  setegid,
  seteuid,
  setgid,
  setgroups,
  setuid,
  permission,
  mainModule,
  _events,
  _eventsCount,
  _exiting,
  _maxListeners,
  _debugEnd,
  _debugProcess,
  _fatalException,
  _getActiveHandles,
  _getActiveRequests,
  _kill,
  _preload_modules,
  _rawDebug,
  _startProfilerIdleNotifier,
  _stopProfilerIdleNotifier,
  _tickCallback,
  _disconnect,
  _handleQueue,
  _pendingMessage,
  _channel,
  _send,
  _linkedBinding
};
var process_default = _process;

// ../tools/tools_npm/node_modules/wrangler/_virtual_unenv_global_polyfill-@cloudflare-unenv-preset-node-process
globalThis.process = process_default;

// _lib/http.js
function json(data, status = 200, extraHeaders = {}) {
  return new Response(JSON.stringify(data), {
    status,
    headers: {
      "content-type": "application/json; charset=utf-8",
      "cache-control": "no-store",
      ...extraHeaders
    }
  });
}
__name(json, "json");
function bad(message, status = 400) {
  return json({ ok: false, error: message }, status);
}
__name(bad, "bad");
function purchasesDisabled() {
  return bad("\u041F\u043E\u043A\u0443\u043F\u043A\u0438 \u0432\u0440\u0435\u043C\u0435\u043D\u043D\u043E \u043E\u0442\u043A\u043B\u044E\u0447\u0435\u043D\u044B", 403);
}
__name(purchasesDisabled, "purchasesDisabled");
async function readJson(request) {
  try {
    return await request.json();
  } catch {
    return null;
  }
}
__name(readJson, "readJson");

// _lib/auth.js
var COOKIE = "at_session";
var SESSION_DAYS = 30;
function b64(buf) {
  const bytes = new Uint8Array(buf);
  let s = "";
  for (let i = 0; i < bytes.length; i++) s += String.fromCharCode(bytes[i]);
  return btoa(s).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/g, "");
}
__name(b64, "b64");
function fromB64(str) {
  const pad = str.length % 4 === 0 ? "" : "=".repeat(4 - str.length % 4);
  const b64s = (str + pad).replace(/-/g, "+").replace(/_/g, "/");
  const bin = atob(b64s);
  const out = new Uint8Array(bin.length);
  for (let i = 0; i < bin.length; i++) out[i] = bin.charCodeAt(i);
  return out;
}
__name(fromB64, "fromB64");
async function hashPassword(password, saltB64) {
  const salt = saltB64 ? fromB64(saltB64) : crypto.getRandomValues(new Uint8Array(16));
  const key = await crypto.subtle.importKey(
    "raw",
    new TextEncoder().encode(password),
    "PBKDF2",
    false,
    ["deriveBits"]
  );
  const bits = await crypto.subtle.deriveBits(
    // Keep iterations modest: Workers CPU budget on free plan is tight.
    { name: "PBKDF2", hash: "SHA-256", salt, iterations: 31e3 },
    key,
    256
  );
  return { hash: b64(bits), salt: b64(salt) };
}
__name(hashPassword, "hashPassword");
async function verifyPassword(password, hash, salt) {
  const again = await hashPassword(password, salt);
  return again.hash === hash;
}
__name(verifyPassword, "verifyPassword");
function newSessionId() {
  return b64(crypto.getRandomValues(new Uint8Array(32)));
}
__name(newSessionId, "newSessionId");
function sessionCookie(id, maxAgeSec = SESSION_DAYS * 86400) {
  const secure = "Secure; ";
  return `${COOKIE}=${id}; Path=/; HttpOnly; ${secure}SameSite=Lax; Max-Age=${maxAgeSec}`;
}
__name(sessionCookie, "sessionCookie");
function clearSessionCookie() {
  return `${COOKIE}=; Path=/; HttpOnly; Secure; SameSite=Lax; Max-Age=0`;
}
__name(clearSessionCookie, "clearSessionCookie");
function getSessionId(request) {
  const raw = request.headers.get("cookie") || "";
  const m = raw.match(/(?:^|;\s*)at_session=([^;]+)/);
  return m ? decodeURIComponent(m[1]) : null;
}
__name(getSessionId, "getSessionId");
function adminNickSet(env2) {
  return new Set(
    String(env2?.ADMIN_NICKS || "").split(",").map((s) => s.trim().toLowerCase()).filter(Boolean)
  );
}
__name(adminNickSet, "adminNickSet");
async function requireUser(db, request) {
  const sid = getSessionId(request);
  if (!sid) return null;
  const row = await db.prepare(
    `SELECT u.id, u.nick, s.expires_at
       FROM sessions s
       JOIN users u ON u.id = s.user_id
       WHERE s.id = ?`
  ).bind(sid).first();
  if (!row) return null;
  if (new Date(row.expires_at).getTime() < Date.now()) {
    await db.prepare("DELETE FROM sessions WHERE id = ?").bind(sid).run();
    return null;
  }
  return { id: row.id, nick: row.nick, sessionId: sid };
}
__name(requireUser, "requireUser");
async function requireAdmin(db, request, env2) {
  const user = await requireUser(db, request);
  if (!user) return null;
  if (adminNickSet(env2).has(String(user.nick).toLowerCase())) {
    return { ...user, is_admin: true };
  }
  try {
    const row = await db.prepare("SELECT is_admin FROM users WHERE id = ?").bind(user.id).first();
    if (Number(row?.is_admin) === 1) return { ...user, is_admin: true };
  } catch {
  }
  return null;
}
__name(requireAdmin, "requireAdmin");
async function userIsAdmin(db, nick, env2) {
  if (adminNickSet(env2).has(String(nick || "").toLowerCase())) return true;
  try {
    const row = await db.prepare("SELECT is_admin FROM users WHERE nick = ? COLLATE NOCASE").bind(nick).first();
    return Number(row?.is_admin) === 1;
  } catch {
    return false;
  }
}
__name(userIsAdmin, "userIsAdmin");
function normalizeNick(nick) {
  return String(nick || "").trim().replace(/\s+/g, "_").slice(0, 16);
}
__name(normalizeNick, "normalizeNick");
function nickOk(nick) {
  return /^[A-Za-z0-9_]{3,16}$/.test(nick);
}
__name(nickOk, "nickOk");
function sessionExpiryIso() {
  const d = new Date(Date.now() + SESSION_DAYS * 86400 * 1e3);
  return d.toISOString();
}
__name(sessionExpiryIso, "sessionExpiryIso");

// _lib/settings.js
var ensured = false;
async function ensureSettings(db) {
  if (ensured) return;
  await db.prepare(
    `CREATE TABLE IF NOT EXISTS site_settings (
        key TEXT PRIMARY KEY,
        value TEXT NOT NULL
      )`
  ).run();
  ensured = true;
}
__name(ensureSettings, "ensureSettings");
async function getSetting(db, key, fallback = "") {
  try {
    await ensureSettings(db);
    const row = await db.prepare("SELECT value FROM site_settings WHERE key = ?").bind(key).first();
    if (row && row.value != null) return String(row.value);
  } catch {
  }
  return fallback;
}
__name(getSetting, "getSetting");
async function setSetting(db, key, value) {
  await ensureSettings(db);
  await db.prepare(
    `INSERT INTO site_settings (key, value) VALUES (?, ?)
       ON CONFLICT(key) DO UPDATE SET value = excluded.value`
  ).bind(key, String(value)).run();
}
__name(setSetting, "setSetting");
async function purchasesEnabled(env2) {
  const fromDb = await getSetting(env2.DB, "purchases_enabled", "");
  if (fromDb !== "") return fromDb.toLowerCase() === "true";
  return String(env2.PURCHASES_ENABLED || "false").toLowerCase() === "true";
}
__name(purchasesEnabled, "purchasesEnabled");

// api/admin/catalog/[id].js
async function onRequestPatch(context2) {
  const { request, env: env2, params } = context2;
  if (!env2.DB) return bad("\u0411\u0430\u0437\u0430 \u043D\u0435 \u043F\u043E\u0434\u043A\u043B\u044E\u0447\u0435\u043D\u0430", 503);
  const admin = await requireAdmin(env2.DB, request, env2);
  if (!admin) return bad("\u041D\u0435\u0442 \u0434\u043E\u0441\u0442\u0443\u043F\u0430", 403);
  const id = Number(params?.id);
  if (!Number.isFinite(id) || id < 1) return bad("\u041D\u0435\u0432\u0435\u0440\u043D\u044B\u0439 id");
  const body = await readJson(request);
  if (!body || typeof body !== "object") return bad("\u041D\u0443\u0436\u0435\u043D JSON");
  const row = await env2.DB.prepare("SELECT id FROM catalog_items WHERE id = ?").bind(id).first();
  if (!row) return bad("\u041D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D\u043E", 404);
  const fields = [];
  const binds = [];
  if ("title" in body) {
    fields.push("title = ?");
    binds.push(String(body.title || "").slice(0, 80));
  }
  if ("description" in body) {
    fields.push("description = ?");
    binds.push(String(body.description || "").slice(0, 500));
  }
  if ("price_rub" in body) {
    const price = Number(body.price_rub);
    if (!Number.isFinite(price) || price < 0) return bad("\u0426\u0435\u043D\u0430 \u0434\u043E\u043B\u0436\u043D\u0430 \u0431\u044B\u0442\u044C \u0447\u0438\u0441\u043B\u043E\u043C \u2265 0");
    fields.push("price_rub = ?");
    binds.push(Math.floor(price));
  }
  if ("enabled" in body) {
    fields.push("enabled = ?");
    binds.push(body.enabled ? 1 : 0);
  }
  if ("sort_order" in body) {
    const sort = Number(body.sort_order);
    if (!Number.isFinite(sort)) return bad("sort_order \u0434\u043E\u043B\u0436\u0435\u043D \u0431\u044B\u0442\u044C \u0447\u0438\u0441\u043B\u043E\u043C");
    fields.push("sort_order = ?");
    binds.push(Math.floor(sort));
  }
  if ("perks" in body) {
    if (!Array.isArray(body.perks)) return bad("perks: \u043C\u0430\u0441\u0441\u0438\u0432 \u0441\u0442\u0440\u043E\u043A");
    fields.push("perks_json = ?");
    binds.push(JSON.stringify(body.perks.map((p) => String(p).slice(0, 120)).slice(0, 20)));
  }
  if (!fields.length) return bad("\u041D\u0435\u0447\u0435\u0433\u043E \u043E\u0431\u043D\u043E\u0432\u043B\u044F\u0442\u044C");
  binds.push(id);
  await env2.DB.prepare(`UPDATE catalog_items SET ${fields.join(", ")} WHERE id = ?`).bind(...binds).run();
  await setSetting(env2.DB, "catalog_from_db", "1");
  const updated = await env2.DB.prepare(
    `SELECT id, kind, slug, title, price_rub, description, perks_json, enabled, sort_order
     FROM catalog_items WHERE id = ?`
  ).bind(id).first();
  let perks = [];
  try {
    perks = JSON.parse(updated.perks_json || "[]");
  } catch {
    perks = [];
  }
  return json({
    ok: true,
    item: {
      id: updated.id,
      kind: updated.kind,
      slug: updated.slug,
      title: updated.title,
      price_rub: updated.price_rub,
      description: updated.description,
      perks,
      enabled: Number(updated.enabled) === 1,
      sort_order: updated.sort_order
    }
  });
}
__name(onRequestPatch, "onRequestPatch");

// _lib/news.js
var DEFAULT_NEWS = [
  {
    title: "\u041B\u0430\u0443\u043D\u0447\u0435\u0440 2.9.20",
    body: "\u041F\u043E\u043B\u043D\u043E\u044D\u043A\u0440\u0430\u043D\u043D\u044B\u0439 \u0432\u0445\u043E\u0434, \u043F\u0430\u043B\u0438\u0442\u0440\u0430 v2, \u0430\u043D\u0438\u043C\u0430\u0446\u0438\u0438 \u043A\u043D\u043E\u043F\u043E\u043A \u0438 \u043C\u044F\u0433\u043A\u0438\u0435 \u0437\u0432\u0443\u043A\u0438 \u043A\u043B\u0438\u043A\u0430.",
    published_at: "2026-08-08"
  },
  {
    title: "\u041F\u043E\u0434\u043A\u043B\u044E\u0447\u0435\u043D\u0438\u0435 \u043A \u0441\u0435\u0440\u0432\u0435\u0440\u0443",
    body: "\u0417\u0430\u0445\u043E\u0434\u0438 \u043F\u043E IP \u0441 \u0441\u0430\u0439\u0442\u0430. \u041E\u0442\u0434\u0435\u043B\u044C\u043D\u044B\u0439 \u0442\u0443\u043D\u043D\u0435\u043B\u044C \u0434\u043B\u044F \u043C\u043E\u0434\u043E\u0432 \u0431\u043E\u043B\u044C\u0448\u0435 \u043D\u0435 \u043D\u0443\u0436\u0435\u043D.",
    published_at: "2026-08-01"
  },
  {
    title: "\u0410\u0432\u0442\u043E\u0440\u044B\u0431\u0430\u043B\u043A\u0430 + StarCatcher",
    body: "\u0423\u0434\u043E\u0447\u043A\u0438 \u0441 \u043A\u0430\u0441\u0442\u043E\u043C\u043D\u044B\u043C \u043B\u0443\u0442\u043E\u043C \u0438 \u0430\u0432\u0442\u043E\u0440\u044B\u0431\u0430\u043B\u043A\u043E\u0439 \u043D\u0430 \u0441\u0435\u0440\u0432\u0435\u0440\u0435.",
    published_at: "2026-07-15"
  }
];
var ensured2 = false;
async function ensureNews(db) {
  if (ensured2) return;
  await db.prepare(
    `CREATE TABLE IF NOT EXISTS news (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        title TEXT NOT NULL,
        body TEXT NOT NULL,
        published_at TEXT NOT NULL,
        published INTEGER NOT NULL DEFAULT 1,
        created_at TEXT NOT NULL DEFAULT (datetime('now')),
        updated_at TEXT NOT NULL DEFAULT (datetime('now'))
      )`
  ).run();
  await db.prepare(
    `CREATE INDEX IF NOT EXISTS idx_news_published ON news (published, published_at DESC)`
  ).run();
  const row = await db.prepare("SELECT COUNT(*) AS n FROM news").first();
  if (!row || Number(row.n) === 0) {
    for (const item of DEFAULT_NEWS) {
      await db.prepare(
        `INSERT INTO news (title, body, published_at, published) VALUES (?, ?, ?, 1)`
      ).bind(item.title, item.body, item.published_at).run();
    }
  }
  ensured2 = true;
}
__name(ensureNews, "ensureNews");
function mapNewsRow(row) {
  return {
    id: Number(row.id),
    title: String(row.title || ""),
    body: String(row.body || ""),
    published_at: String(row.published_at || ""),
    published: Number(row.published) === 1,
    created_at: row.created_at ? String(row.created_at) : null,
    updated_at: row.updated_at ? String(row.updated_at) : null
  };
}
__name(mapNewsRow, "mapNewsRow");
async function listNews(db, { publishedOnly = true, limit = 50 } = {}) {
  await ensureNews(db);
  const cap = Math.min(Math.max(Number(limit) || 50, 1), 100);
  const sql = publishedOnly ? `SELECT * FROM news WHERE published = 1 ORDER BY published_at DESC, id DESC LIMIT ?` : `SELECT * FROM news ORDER BY published_at DESC, id DESC LIMIT ?`;
  const { results } = await db.prepare(sql).bind(cap).all();
  return (results || []).map(mapNewsRow);
}
__name(listNews, "listNews");
async function getNewsById(db, id) {
  await ensureNews(db);
  const row = await db.prepare("SELECT * FROM news WHERE id = ?").bind(id).first();
  return row ? mapNewsRow(row) : null;
}
__name(getNewsById, "getNewsById");

// api/admin/news/[id].js
async function onRequestPatch2(context2) {
  const { request, env: env2, params } = context2;
  if (!env2.DB) return bad("\u0411\u0430\u0437\u0430 \u043D\u0435 \u043F\u043E\u0434\u043A\u043B\u044E\u0447\u0435\u043D\u0430", 503);
  const admin = await requireAdmin(env2.DB, request, env2);
  if (!admin) return bad("\u041D\u0435\u0442 \u0434\u043E\u0441\u0442\u0443\u043F\u0430", 403);
  const id = Number(params?.id);
  if (!Number.isFinite(id) || id < 1) return bad("\u041D\u0435\u0432\u0435\u0440\u043D\u044B\u0439 id");
  const body = await readJson(request);
  if (!body || typeof body !== "object") return bad("\u041D\u0443\u0436\u0435\u043D JSON");
  await ensureNews(env2.DB);
  const current = await getNewsById(env2.DB, id);
  if (!current) return bad("\u041D\u043E\u0432\u043E\u0441\u0442\u044C \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D\u0430", 404);
  const title2 = "title" in body ? String(body.title || "").trim().slice(0, 160) : current.title;
  const text = "body" in body ? String(body.body || "").trim().slice(0, 4e3) : current.body;
  const published_at = "published_at" in body ? String(body.published_at || "").trim().slice(0, 32) : current.published_at;
  const published = "published" in body ? body.published === false || body.published === 0 ? 0 : 1 : current.published ? 1 : 0;
  if (title2.length < 2) return bad("\u041D\u0443\u0436\u0435\u043D \u0437\u0430\u0433\u043E\u043B\u043E\u0432\u043E\u043A");
  if (text.length < 2) return bad("\u041D\u0443\u0436\u0435\u043D \u0442\u0435\u043A\u0441\u0442");
  if (!published_at) return bad("\u041D\u0443\u0436\u043D\u0430 \u0434\u0430\u0442\u0430");
  await env2.DB.prepare(
    `UPDATE news
       SET title = ?, body = ?, published_at = ?, published = ?, updated_at = datetime('now')
       WHERE id = ?`
  ).bind(title2, text, published_at, published, id).run();
  const row = await env2.DB.prepare("SELECT * FROM news WHERE id = ?").bind(id).first();
  return json({ ok: true, item: row ? mapNewsRow(row) : null });
}
__name(onRequestPatch2, "onRequestPatch");
async function onRequestDelete(context2) {
  const { request, env: env2, params } = context2;
  if (!env2.DB) return bad("\u0411\u0430\u0437\u0430 \u043D\u0435 \u043F\u043E\u0434\u043A\u043B\u044E\u0447\u0435\u043D\u0430", 503);
  const admin = await requireAdmin(env2.DB, request, env2);
  if (!admin) return bad("\u041D\u0435\u0442 \u0434\u043E\u0441\u0442\u0443\u043F\u0430", 403);
  const id = Number(params?.id);
  if (!Number.isFinite(id) || id < 1) return bad("\u041D\u0435\u0432\u0435\u0440\u043D\u044B\u0439 id");
  await ensureNews(env2.DB);
  const current = await getNewsById(env2.DB, id);
  if (!current) return bad("\u041D\u043E\u0432\u043E\u0441\u0442\u044C \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D\u0430", 404);
  await env2.DB.prepare("DELETE FROM news WHERE id = ?").bind(id).run();
  return json({ ok: true, deleted: id });
}
__name(onRequestDelete, "onRequestDelete");

// api/admin/users/[nick].js
async function onRequestPatch3(context2) {
  const { request, env: env2, params } = context2;
  if (!env2.DB) return bad("\u0411\u0430\u0437\u0430 \u043D\u0435 \u043F\u043E\u0434\u043A\u043B\u044E\u0447\u0435\u043D\u0430", 503);
  const admin = await requireAdmin(env2.DB, request, env2);
  if (!admin) return bad("\u041D\u0435\u0442 \u0434\u043E\u0441\u0442\u0443\u043F\u0430", 403);
  const nick = normalizeNick(params?.nick || "");
  if (!nickOk(nick)) return bad("\u041D\u0435\u0432\u0435\u0440\u043D\u044B\u0439 \u043D\u0438\u043A");
  const body = await readJson(request);
  if (!body || typeof body !== "object") return bad("\u041D\u0443\u0436\u0435\u043D JSON");
  const user = await env2.DB.prepare("SELECT id, nick FROM users WHERE nick = ? COLLATE NOCASE").bind(nick).first();
  if (!user) return bad("\u0418\u0433\u0440\u043E\u043A \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D", 404);
  if ("is_admin" in body) {
    try {
      await env2.DB.prepare("UPDATE users SET is_admin = ? WHERE id = ?").bind(body.is_admin ? 1 : 0, user.id).run();
    } catch {
      return bad("\u041A\u043E\u043B\u043E\u043D\u043A\u0430 is_admin \u0435\u0449\u0451 \u043D\u0435 \u0441\u043E\u0437\u0434\u0430\u043D\u0430 (\u043D\u0443\u0436\u043D\u0430 \u043C\u0438\u0433\u0440\u0430\u0446\u0438\u044F 0003)", 503);
    }
  }
  const profileBits = [];
  const binds = [];
  if ("privilege" in body) {
    profileBits.push("privilege = ?");
    binds.push(String(body.privilege || "\u0418\u0433\u0440\u043E\u043A").slice(0, 40));
  }
  if ("bio" in body) {
    profileBits.push("bio = ?");
    binds.push(String(body.bio || "").slice(0, 280));
  }
  for (const key of ["coins", "likes", "fish", "playtime_hours"]) {
    if (key in body) {
      const n = Number(body[key]);
      if (!Number.isFinite(n) || n < 0) return bad(`${key}: \u0447\u0438\u0441\u043B\u043E \u2265 0`);
      profileBits.push(`${key} = ?`);
      binds.push(Math.floor(n));
    }
  }
  if (profileBits.length) {
    binds.push(user.id);
    await env2.DB.prepare(`UPDATE profiles SET ${profileBits.join(", ")} WHERE user_id = ?`).bind(...binds).run();
  }
  let row;
  try {
    row = await env2.DB.prepare(
      `SELECT u.nick, COALESCE(u.is_admin, 0) AS is_admin,
              p.privilege, p.coins, p.likes, p.fish, p.playtime_hours, p.bio
       FROM users u LEFT JOIN profiles p ON p.user_id = u.id
       WHERE u.id = ?`
    ).bind(user.id).first();
  } catch {
    row = await env2.DB.prepare(
      `SELECT u.nick, 0 AS is_admin,
              p.privilege, p.coins, p.likes, p.fish, p.playtime_hours, p.bio
       FROM users u LEFT JOIN profiles p ON p.user_id = u.id
       WHERE u.id = ?`
    ).bind(user.id).first();
  }
  return json({
    ok: true,
    user: {
      nick: row.nick,
      is_admin: Number(row.is_admin) === 1,
      privilege: row.privilege || "\u0418\u0433\u0440\u043E\u043A",
      coins: row.coins || 0,
      likes: row.likes || 0,
      fish: row.fish || 0,
      playtime_hours: row.playtime_hours || 0,
      bio: row.bio || ""
    }
  });
}
__name(onRequestPatch3, "onRequestPatch");

// api/profiles/[nick]/like.js
async function onRequestPost(context2) {
  const { request, env: env2, params } = context2;
  if (!env2.DB) return bad("\u0411\u0430\u0437\u0430 \u043D\u0435 \u043F\u043E\u0434\u043A\u043B\u044E\u0447\u0435\u043D\u0430 (D1)", 503);
  const currentUser = await requireUser(env2.DB, request);
  if (!currentUser) return bad("\u0412\u043E\u0439\u0434\u0438\u0442\u0435 \u0432 \u0430\u043A\u043A\u0430\u0443\u043D\u0442, \u0447\u0442\u043E\u0431\u044B \u043F\u043E\u0441\u0442\u0430\u0432\u0438\u0442\u044C \u043B\u0430\u0439\u043A", 401);
  const targetNick = String(params.nick || "").trim();
  if (!targetNick) return bad("\u041D\u0438\u043A \u043D\u0435 \u0443\u043A\u0430\u0437\u0430\u043D");
  const targetUser = await env2.DB.prepare(
    "SELECT id, nick FROM users WHERE nick = ? COLLATE NOCASE"
  ).bind(targetNick).first();
  if (!targetUser) return bad("\u0418\u0433\u0440\u043E\u043A \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D", 404);
  if (currentUser.id === targetUser.id) {
    return bad("\u041D\u0435\u043B\u044C\u0437\u044F \u0441\u0442\u0430\u0432\u0438\u0442\u044C \u043B\u0430\u0439\u043A \u0441\u0432\u043E\u0435\u043C\u0443 \u043F\u0440\u043E\u0444\u0438\u043B\u044E", 400);
  }
  const existing = await env2.DB.prepare(
    "SELECT 1 FROM profile_likes WHERE from_user_id = ? AND to_user_id = ?"
  ).bind(currentUser.id, targetUser.id).first();
  let liked = false;
  if (existing) {
    await env2.DB.batch([
      env2.DB.prepare(
        "DELETE FROM profile_likes WHERE from_user_id = ? AND to_user_id = ?"
      ).bind(currentUser.id, targetUser.id),
      env2.DB.prepare(
        "UPDATE profiles SET likes = MAX(0, likes - 1), updated_at = strftime('%Y-%m-%dT%H:%M:%fZ', 'now') WHERE user_id = ?"
      ).bind(targetUser.id)
    ]);
    liked = false;
  } else {
    await env2.DB.batch([
      env2.DB.prepare(
        "INSERT INTO profile_likes (from_user_id, to_user_id) VALUES (?, ?)"
      ).bind(currentUser.id, targetUser.id),
      env2.DB.prepare(
        "UPDATE profiles SET likes = likes + 1, updated_at = strftime('%Y-%m-%dT%H:%M:%fZ', 'now') WHERE user_id = ?"
      ).bind(targetUser.id)
    ]);
    liked = true;
  }
  const updatedProfile = await env2.DB.prepare(
    "SELECT likes FROM profiles WHERE user_id = ?"
  ).bind(targetUser.id).first();
  return json({
    ok: true,
    liked,
    likes: Number(updatedProfile?.likes ?? 0)
  });
}
__name(onRequestPost, "onRequestPost");

// api/admin/catalog.js
var SHORT = {
  vip: {
    description: "\u041F\u0440\u0435\u0444\u0438\u043A\u0441, \u0446\u0432\u0435\u0442\u043D\u043E\u0439 \u043D\u0438\u043A, +1 \u0434\u043E\u043C. \u041A\u0443\u043F\u0438\u0442\u044C \u043D\u0430 \u0441\u0430\u0439\u0442\u0435 \u043F\u043E\u043A\u0430 \u043D\u0435\u043B\u044C\u0437\u044F.",
    perks: ["\u041F\u0440\u0435\u0444\u0438\u043A\u0441 VIP \u0432 \u0447\u0430\u0442\u0435", "+1 \u0434\u043E\u043C /sethome", "\u0426\u0432\u0435\u0442\u043D\u043E\u0439 \u043D\u0438\u043A", "\u041F\u0440\u0438\u043E\u0440\u0438\u0442\u0435\u0442 \u0432 \u043E\u0447\u0435\u0440\u0435\u0434\u0438"]
  },
  premium: {
    description: "\u0412\u0441\u0451 \u0438\u0437 VIP, \u043A\u0435\u0439\u0441 \u0432 \u0434\u0435\u043D\u044C \u043D\u0430 \u0441\u0435\u0440\u0432\u0435\u0440\u0435, \u043F\u0440\u0438\u043E\u0440\u0438\u0442\u0435\u0442 \u0432\u0445\u043E\u0434\u0430.",
    perks: ["\u0412\u0441\u0451 \u0438\u0437 VIP", "\u041A\u0435\u0439\u0441 \u0432 \u0434\u0435\u043D\u044C (\u0432 \u0438\u0433\u0440\u0435)", "\u041F\u0440\u0438\u043E\u0440\u0438\u0442\u0435\u0442 \u0432\u0445\u043E\u0434\u0430", "\u0414\u043E\u043F. \u0441\u043B\u043E\u0442 \u0432\u0430\u0440\u043F\u0430"]
  },
  deluxe: {
    description: "\u0411\u043E\u043D\u0443\u0441 \u043A \u0443\u043B\u043E\u0432\u0443 \u0438 \u0440\u0430\u043C\u043A\u0430 \u043F\u0440\u043E\u0444\u0438\u043B\u044F. \u041E\u043F\u043B\u0430\u0442\u0430 \u043D\u0430 \u0441\u0430\u0439\u0442\u0435 \u0432\u044B\u043A\u043B\u044E\u0447\u0435\u043D\u0430.",
    perks: ["\u0412\u0441\u0451 \u0438\u0437 Premium", "\u0420\u0430\u043C\u043A\u0430 \u043F\u0440\u043E\u0444\u0438\u043B\u044F", "\u0411\u043E\u043D\u0443\u0441 \u043A \u0443\u043B\u043E\u0432\u0443", "\u0411\u0435\u0439\u0434\u0436 Deluxe"]
  },
  ultimate: {
    description: "\u041C\u0430\u043A\u0441\u0438\u043C\u0443\u043C \u043F\u0440\u0438\u0432\u0438\u043B\u0435\u0433\u0438\u0439 \u043D\u0430 \u0441\u0435\u0440\u0432\u0435\u0440\u0435. \u041E\u043F\u043B\u0430\u0442\u0430 \u043D\u0430 \u0441\u0430\u0439\u0442\u0435 \u043F\u043E\u0437\u0436\u0435.",
    perks: ["\u0412\u0441\u0451 \u0438\u0437 Deluxe", "\u0411\u0435\u0439\u0434\u0436 Ultimate", "\u041C\u0430\u043A\u0441\u0438\u043C\u0443\u043C \u0434\u043E\u043C\u043E\u0432", "\u041F\u0440\u0438\u043E\u0440\u0438\u0442\u0435\u0442 \u0432 \u043F\u043E\u0434\u0434\u0435\u0440\u0436\u043A\u0435"]
  },
  ocean: {
    description: "\u041C\u043E\u043D\u0435\u0442\u044B \u0438 \u0440\u0430\u0441\u0445\u043E\u0434\u043D\u0438\u043A\u0438. \u041E\u0442\u043A\u0440\u044B\u0432\u0430\u0435\u0442\u0441\u044F \u0432 \u0438\u0433\u0440\u0435 (F4).",
    perks: ["AquaCoins", "\u0420\u0430\u0441\u0445\u043E\u0434\u043D\u0438\u043A\u0438", "\u041C\u0435\u043B\u043A\u0438\u0439 \u0431\u0443\u0441\u0442"]
  },
  fisher: {
    description: "\u041B\u0443\u0442 \u043F\u043E\u0434 StarCatcher. \u0420\u0443\u043B\u0435\u0442\u043A\u0438 \u043D\u0430 \u0441\u0430\u0439\u0442\u0435 \u043D\u0435\u0442.",
    perks: ["\u0420\u0435\u0441\u0443\u0440\u0441\u044B \u0443\u043B\u043E\u0432\u0430", "\u0411\u0443\u0441\u0442 \u0443\u0434\u043E\u0447\u043A\u0438", "\u041C\u043E\u043D\u0435\u0442\u044B"]
  },
  depth: {
    description: "\u0420\u0435\u0434\u043A\u0430\u044F \u043A\u043E\u0441\u043C\u0435\u0442\u0438\u043A\u0430 \u0438 \u043F\u0440\u043E\u0431\u043D\u044B\u0435 \u043F\u0440\u0438\u0432\u0438\u043B\u0435\u0433\u0438\u0438. \u0422\u043E\u043B\u044C\u043A\u043E \u0441\u0435\u0440\u0432\u0435\u0440.",
    perks: ["\u0420\u0430\u043C\u043A\u0430 \u043F\u0440\u043E\u0444\u0438\u043B\u044F", "\u041F\u0440\u043E\u0431\u043D\u0430\u044F \u043F\u0440\u0438\u0432\u0438\u043B\u0435\u0433\u0438\u044F", "\u041A\u0440\u0443\u043F\u043D\u044B\u0439 \u0437\u0430\u043F\u0430\u0441 \u043C\u043E\u043D\u0435\u0442"]
  }
};
function parsePerks(raw) {
  try {
    const v = JSON.parse(raw || "[]");
    return Array.isArray(v) ? v.map(String) : [];
  } catch {
    return [];
  }
}
__name(parsePerks, "parsePerks");
function mapRow(row) {
  return {
    id: row.id,
    kind: row.kind,
    slug: row.slug,
    title: row.title,
    price_rub: row.price_rub,
    description: row.description,
    perks: parsePerks(row.perks_json),
    enabled: Number(row.enabled) === 1,
    sort_order: row.sort_order
  };
}
__name(mapRow, "mapRow");
async function onRequestGet(context2) {
  const { request, env: env2 } = context2;
  if (!env2.DB) return bad("\u0411\u0430\u0437\u0430 \u043D\u0435 \u043F\u043E\u0434\u043A\u043B\u044E\u0447\u0435\u043D\u0430", 503);
  const admin = await requireAdmin(env2.DB, request, env2);
  if (!admin) return bad("\u041D\u0435\u0442 \u0434\u043E\u0441\u0442\u0443\u043F\u0430", 403);
  const res = await env2.DB.prepare(
    `SELECT id, kind, slug, title, price_rub, description, perks_json, enabled, sort_order
     FROM catalog_items ORDER BY kind ASC, sort_order ASC, id ASC`
  ).all();
  return json({ ok: true, items: (res.results || []).map(mapRow) });
}
__name(onRequestGet, "onRequestGet");
async function onRequestPost2(context2) {
  const { request, env: env2 } = context2;
  if (!env2.DB) return bad("\u0411\u0430\u0437\u0430 \u043D\u0435 \u043F\u043E\u0434\u043A\u043B\u044E\u0447\u0435\u043D\u0430", 503);
  const admin = await requireAdmin(env2.DB, request, env2);
  if (!admin) return bad("\u041D\u0435\u0442 \u0434\u043E\u0441\u0442\u0443\u043F\u0430", 403);
  const body = await readJson(request);
  if (body?.action !== "short_copy") return bad("\u041D\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043D\u043E\u0435 \u0434\u0435\u0439\u0441\u0442\u0432\u0438\u0435");
  for (const [slug, copy] of Object.entries(SHORT)) {
    await env2.DB.prepare(
      `UPDATE catalog_items SET description = ?, perks_json = ? WHERE slug = ?`
    ).bind(copy.description, JSON.stringify(copy.perks), slug).run();
  }
  await setSetting(env2.DB, "catalog_from_db", "1");
  return onRequestGet(context2);
}
__name(onRequestPost2, "onRequestPost");

// api/admin/me.js
async function onRequestGet2(context2) {
  const { request, env: env2 } = context2;
  if (!env2.DB) return bad("\u0411\u0430\u0437\u0430 \u043D\u0435 \u043F\u043E\u0434\u043A\u043B\u044E\u0447\u0435\u043D\u0430", 503);
  const admin = await requireAdmin(env2.DB, request, env2);
  if (!admin) return bad("\u041D\u0435\u0442 \u0434\u043E\u0441\u0442\u0443\u043F\u0430", 403);
  return json({ ok: true, user: { nick: admin.nick, is_admin: true } });
}
__name(onRequestGet2, "onRequestGet");

// api/admin/news.js
async function onRequestGet3(context2) {
  const { request, env: env2 } = context2;
  if (!env2.DB) return bad("\u0411\u0430\u0437\u0430 \u043D\u0435 \u043F\u043E\u0434\u043A\u043B\u044E\u0447\u0435\u043D\u0430", 503);
  const admin = await requireAdmin(env2.DB, request, env2);
  if (!admin) return bad("\u041D\u0435\u0442 \u0434\u043E\u0441\u0442\u0443\u043F\u0430", 403);
  const news = await listNews(env2.DB, { publishedOnly: false, limit: 100 });
  return json({ ok: true, news });
}
__name(onRequestGet3, "onRequestGet");
async function onRequestPost3(context2) {
  const { request, env: env2 } = context2;
  if (!env2.DB) return bad("\u0411\u0430\u0437\u0430 \u043D\u0435 \u043F\u043E\u0434\u043A\u043B\u044E\u0447\u0435\u043D\u0430", 503);
  const admin = await requireAdmin(env2.DB, request, env2);
  if (!admin) return bad("\u041D\u0435\u0442 \u0434\u043E\u0441\u0442\u0443\u043F\u0430", 403);
  const body = await readJson(request);
  if (!body || typeof body !== "object") return bad("\u041D\u0443\u0436\u0435\u043D JSON");
  const title2 = String(body.title || "").trim().slice(0, 160);
  const text = String(body.body || "").trim().slice(0, 4e3);
  const published_at = String(body.published_at || (/* @__PURE__ */ new Date()).toISOString().slice(0, 10)).slice(0, 32);
  const published = body.published === false || body.published === 0 ? 0 : 1;
  if (title2.length < 2) return bad("\u041D\u0443\u0436\u0435\u043D \u0437\u0430\u0433\u043E\u043B\u043E\u0432\u043E\u043A");
  if (text.length < 2) return bad("\u041D\u0443\u0436\u0435\u043D \u0442\u0435\u043A\u0441\u0442");
  await ensureNews(env2.DB);
  const result = await env2.DB.prepare(
    `INSERT INTO news (title, body, published_at, published) VALUES (?, ?, ?, ?)
       RETURNING *`
  ).bind(title2, text, published_at, published).first();
  if (!result) {
    const info3 = await env2.DB.prepare(`INSERT INTO news (title, body, published_at, published) VALUES (?, ?, ?, ?)`).bind(title2, text, published_at, published).run();
    const id = info3?.meta?.last_row_id;
    const row = id ? await env2.DB.prepare("SELECT * FROM news WHERE id = ?").bind(id).first() : null;
    return json({ ok: true, item: row ? mapNewsRow(row) : null }, 201);
  }
  return json({ ok: true, item: mapNewsRow(result) }, 201);
}
__name(onRequestPost3, "onRequestPost");

// _lib/siteCopy.js
var SITE_COPY_KEYS = [
  "hero_eyebrow",
  "hero_title",
  "hero_lead",
  "features_title",
  "features_lead",
  "tile_rods_tag",
  "tile_rods_title",
  "tile_rods_body",
  "tile_cases_tag",
  "tile_cases_title",
  "tile_cases_body",
  "tile_top_tag",
  "tile_top_title",
  "tile_top_body",
  "home_news_title",
  "home_news_lead",
  "join_title",
  "join_body",
  "footer_blurb",
  "start_eyebrow",
  "start_title",
  "start_lead",
  "start_step1_title",
  "start_step1_body",
  "start_step2_title",
  "start_step2_1",
  "start_step2_2",
  "start_step2_3",
  "start_step2_4",
  "store_eyebrow",
  "store_title",
  "store_lead",
  "store_notice",
  "cases_eyebrow",
  "cases_title",
  "cases_lead",
  "cases_notice",
  "rods_eyebrow",
  "rods_title",
  "rods_lead",
  "rods_rules_title",
  "rods_rule_1",
  "rods_rule_2",
  "rods_rule_3",
  "rods_rule_4",
  "top_eyebrow",
  "top_title",
  "top_lead",
  "news_eyebrow",
  "news_title",
  "news_page_lead",
  "profile_eyebrow",
  "profile_title",
  "profile_lead",
  "login_eyebrow",
  "login_title",
  "login_lead",
  "register_eyebrow",
  "register_title",
  "register_lead",
  "players_eyebrow",
  "players_title",
  "players_lead",
  "rules_eyebrow",
  "rules_title",
  "rules_1",
  "rules_2",
  "rules_3",
  "rules_4",
  "rules_5"
];
var SITE_COPY_DEFAULTS = {
  hero_eyebrow: "Minecraft 1.20.1 \xB7 \u043E\u043A\u0435\u0430\u043D\u0441\u043A\u0438\u0439 skyblock",
  hero_title: "AquaTech",
  hero_lead: "\u0421\u043F\u0430\u0432\u043D \u043D\u0430 \u043F\u043B\u043E\u0442\u0443. \u0414\u0432\u0435\u043D\u0430\u0434\u0446\u0430\u0442\u044C \u0443\u0434\u043E\u0447\u0435\u043A StarCatcher, \u0430\u0432\u0442\u043E\u0440\u044B\u0431\u0430\u043B\u043A\u0430, \u043A\u0435\u0439\u0441\u044B \u0438 \u0438\u043D\u0434\u0443\u0441\u0442\u0440\u0438\u0430\u043B\u044C\u043D\u044B\u0435 \u043C\u043E\u0434\u044B. \u0421\u043A\u0430\u0447\u0430\u0439 \u043B\u0430\u0443\u043D\u0447\u0435\u0440 \u0438 \u0437\u0430\u0445\u043E\u0434\u0438.",
  features_title: "\u041D\u0430 \u0441\u0435\u0440\u0432\u0435\u0440\u0435",
  features_lead: "\u041E\u0434\u0438\u043D \u043C\u0438\u0440-\u043E\u043A\u0435\u0430\u043D. \u0420\u044B\u0431\u0430\u043B\u043A\u0430, \u043A\u0435\u0439\u0441\u044B, \u043F\u0440\u043E\u0433\u0440\u0435\u0441\u0441\u0438\u044F.",
  tile_rods_tag: "\u0423\u0434\u043E\u0447\u043A\u0438",
  tile_rods_title: "StarCatcher",
  tile_rods_body: "\u0411\u0430\u043C\u0431\u0443\u043A \u0432 \u043D\u0430\u0447\u0430\u043B\u0435, \u0434\u0430\u043B\u044C\u0448\u0435 \u0440\u0443\u0434\u044B \u0438 \u0438\u043D\u0434\u0443\u0441\u0442\u0440\u0438\u0430\u043B\u044C\u043D\u044B\u0439 \u043B\u0443\u0442 \u0438\u0437 \u043F\u0443\u043B\u043E\u0432 AquaTech.",
  tile_cases_tag: "\u041A\u0435\u0439\u0441\u044B",
  tile_cases_title: "\u041D\u0430\u0433\u0440\u0430\u0434\u044B \u0432 \u0438\u0433\u0440\u0435",
  tile_cases_body: "\u041A\u0435\u0439\u0441\u044B \u043A\u0440\u0443\u0442\u044F\u0442\u0441\u044F \u043D\u0430 \u0441\u0435\u0440\u0432\u0435\u0440\u0435 (F4). \u041D\u0430 \u0441\u0430\u0439\u0442\u0435 \u0442\u043E\u043B\u044C\u043A\u043E \u0441\u043E\u0441\u0442\u0430\u0432.",
  tile_top_tag: "\u0418\u0433\u0440\u043E\u043A\u0438",
  tile_top_title: "\u0422\u043E\u043F\u044B",
  tile_top_body: "\u0420\u0435\u0439\u0442\u0438\u043D\u0433 \u043F\u043E \u043B\u0430\u0439\u043A\u0430\u043C \u0438 \u043C\u043E\u043D\u0435\u0442\u0430\u043C. \u041F\u0440\u043E\u0444\u0438\u043B\u044C \u043C\u043E\u0436\u043D\u043E \u043E\u0444\u043E\u0440\u043C\u0438\u0442\u044C \u043F\u043E\u0441\u043B\u0435 \u0432\u0445\u043E\u0434\u0430.",
  home_news_title: "\u041D\u043E\u0432\u043E\u0441\u0442\u0438",
  home_news_lead: "\u0427\u0442\u043E \u043C\u0435\u043D\u044F\u043B\u043E\u0441\u044C \u0432 \u043B\u0430\u0443\u043D\u0447\u0435\u0440\u0435 \u0438 \u043D\u0430 \u0441\u0435\u0440\u0432\u0435\u0440\u0435.",
  join_title: "AquaTech Ocean",
  join_body: "\u041E\u043A\u0435\u0430\u043D\u0441\u043A\u0438\u0439 skyblock, \u043F\u043B\u043E\u0442 4\xD74. \u0417\u0430\u0445\u043E\u0434\u0438 \u043F\u043E IP \u043D\u0438\u0436\u0435.",
  footer_blurb: "\u041E\u043A\u0435\u0430\u043D\u0441\u043A\u0438\u0439 \u0441\u0435\u0440\u0432\u0435\u0440. \u0421\u043A\u0430\u0447\u0430\u0439 \u043B\u0430\u0443\u043D\u0447\u0435\u0440 \u0438 \u0437\u0430\u0445\u043E\u0434\u0438.",
  start_eyebrow: "\u0421\u0442\u0430\u0440\u0442",
  start_title: "\u041A\u0430\u043A \u0437\u0430\u0439\u0442\u0438",
  start_lead: "\u041D\u0443\u0436\u0435\u043D Windows. \u0421\u043A\u0430\u0447\u0430\u0439 \u043B\u0430\u0443\u043D\u0447\u0435\u0440, \u0432\u043F\u0438\u0448\u0438 \u043D\u0438\u043A \u0438 \u0436\u043C\u0438 \xAB\u0418\u0433\u0440\u0430\u0442\u044C\xBB.",
  start_step1_title: "1. \u041B\u0430\u0443\u043D\u0447\u0435\u0440",
  start_step1_body: "\u0421\u043A\u0430\u0447\u0430\u0439 \u0438 \u0437\u0430\u043F\u0443\u0441\u0442\u0438. \u0414\u0430\u043B\u044C\u0448\u0435 \u0432\u0441\u0451 \u043F\u043E\u0441\u0442\u0430\u0432\u0438\u0442\u0441\u044F \u0441\u0430\u043C\u043E.",
  start_step2_title: "2. \u0418\u0433\u0440\u0430",
  start_step2_1: "\u0412\u043F\u0438\u0448\u0438 \u043D\u0438\u043A \u0432 \u043B\u0430\u0443\u043D\u0447\u0435\u0440\u0435",
  start_step2_2: "\u0414\u043E\u0436\u0434\u0438\u0441\u044C \u0437\u0430\u0433\u0440\u0443\u0437\u043A\u0438",
  start_step2_3: "\u0416\u043C\u0438 \xAB\u0418\u0433\u0440\u0430\u0442\u044C\xBB",
  start_step2_4: "IP \u0432\u0440\u0443\u0447\u043D\u0443\u044E: g-pl-3.apexnodes.xyz:21561",
  store_eyebrow: "\u041C\u0430\u0433\u0430\u0437\u0438\u043D",
  store_title: "\u041F\u0440\u0438\u0432\u0438\u043B\u0435\u0433\u0438\u0438",
  store_lead: "\u0421\u043E\u0441\u0442\u0430\u0432 \u0440\u0430\u043D\u0433\u043E\u0432 \u0438 \u0446\u0435\u043D\u044B. \u041A\u0443\u043F\u0438\u0442\u044C \u043D\u0430 \u0441\u0430\u0439\u0442\u0435 \u043F\u043E\u043A\u0430 \u043D\u0435\u043B\u044C\u0437\u044F.",
  store_notice: "\u041F\u043E\u043A\u0443\u043F\u043A\u0438 \u0432\u044B\u043A\u043B\u044E\u0447\u0435\u043D\u044B. \u041E\u043F\u043B\u0430\u0442\u0443 \u043F\u043E\u0434\u043A\u043B\u044E\u0447\u0438\u043C \u043F\u043E\u0437\u0436\u0435.",
  cases_eyebrow: "\u041A\u0435\u0439\u0441\u044B",
  cases_title: "\u0427\u0442\u043E \u0432\u043D\u0443\u0442\u0440\u0438",
  cases_lead: "\u0421\u0430\u0439\u0442 \u0442\u043E\u043B\u044C\u043A\u043E \u043F\u043E\u043A\u0430\u0437\u044B\u0432\u0430\u0435\u0442 \u0441\u043E\u0441\u0442\u0430\u0432. \u041E\u0442\u043A\u0440\u044B\u0432\u0430\u0439 \u043A\u0435\u0439\u0441\u044B \u0432 \u0438\u0433\u0440\u0435 (F4).",
  cases_notice: "\u041D\u0430 \u0441\u0430\u0439\u0442\u0435 \u043A\u0435\u0439\u0441\u044B \u043D\u0435 \u043E\u0442\u043A\u0440\u044B\u0432\u0430\u044E\u0442\u0441\u044F.",
  rods_eyebrow: "StarCatcher",
  rods_title: "\u0423\u0434\u043E\u0447\u043A\u0438 \u0438 \u043B\u0443\u0442",
  rods_lead: "\u0412\u0430\u043D\u0438\u043B\u044C\u043D\u044B\u0439 \u0443\u043B\u043E\u0432 \u0432\u044B\u043A\u043B\u044E\u0447\u0435\u043D. \u0420\u0435\u0441\u0443\u0440\u0441\u043D\u044B\u0435 \u0443\u0434\u043E\u0447\u043A\u0438 \u043A\u0440\u0443\u0442\u044F\u0442 \u043F\u0443\u043B\u044B AquaTech; \u043C\u043D\u043E\u0436\u0438\u0442\u0435\u043B\u0438 \xD72\u2026\xD764 \u0443\u043C\u043D\u043E\u0436\u0430\u044E\u0442 \u043A\u043E\u043B\u0438\u0447\u0435\u0441\u0442\u0432\u043E.",
  rods_rules_title: "\u041A\u0430\u043A \u0441\u0447\u0438\u0442\u0430\u0435\u0442\u0441\u044F \u0443\u043B\u043E\u0432",
  rods_rule_1: "\u041A\u0430\u0436\u0434\u044B\u0439 \u043F\u0440\u0435\u0434\u043C\u0435\u0442 \u0432 \u043F\u0443\u043B\u0435 \u0441\u043D\u0430\u0447\u0430\u043B\u0430 \u043A\u0438\u0434\u0430\u0435\u0442 \u0441\u0432\u043E\u0439 \u0448\u0430\u043D\u0441.",
  rods_rule_2: "\u0418\u0437 \u0443\u0441\u043F\u0435\u0448\u043D\u043E \u043F\u0440\u043E\u0448\u0435\u0434\u0448\u0438\u0445 \u0441\u043B\u0443\u0447\u0430\u0439\u043D\u043E \u043E\u0441\u0442\u0430\u0432\u043B\u044F\u044E\u0442 1\u20133 \u0441\u0442\u0430\u043A\u0430 (\u0443 T1 \u0434\u043E\u043F. \u043F\u0443\u043B\u0430 \u2014 1\u20132).",
  rods_rule_3: "\u041A\u043E\u043B-\u0432\u043E \u0432 \u0441\u0442\u0430\u043A\u0435 \u2014 \u0434\u0438\u0430\u043F\u0430\u0437\u043E\u043D \u0438\u0437 \u0442\u0430\u0431\u043B\u0438\u0446\u044B; \u043C\u043D\u043E\u0436\u0438\u0442\u0435\u043B\u044C \u0443\u0434\u043E\u0447\u043A\u0438 \u0435\u0433\u043E \u0443\u043C\u043D\u043E\u0436\u0430\u0435\u0442.",
  rods_rule_4: "\u041A\u043E\u0441\u0442\u044F\u043D\u0430\u044F \u0438 \u043D\u0435\u0431\u0435\u0441\u043D\u0430\u044F \u2014 \u0442\u043E\u043B\u044C\u043A\u043E \u0440\u044B\u0431\u0430 StarCatcher, \u0431\u0435\u0437 \u0440\u0435\u0441\u0443\u0440\u0441\u043D\u043E\u0433\u043E \u043F\u0443\u043B\u0430.",
  top_eyebrow: "\u0420\u0435\u0439\u0442\u0438\u043D\u0433\u0438",
  top_title: "\u0422\u043E\u043F\u044B",
  top_lead: "\u041A\u0442\u043E \u0441\u043A\u043E\u043B\u044C\u043A\u043E \u043D\u0430\u0438\u0433\u0440\u0430\u043B, \u043A\u0442\u043E \u043D\u0430\u043A\u043E\u043F\u0438\u043B \u043C\u043E\u043D\u0435\u0442, \u043A\u043E\u0433\u043E \u043B\u0430\u0439\u043A\u043D\u0443\u043B\u0438.",
  news_eyebrow: "\u0411\u043B\u043E\u0433",
  news_title: "\u041D\u043E\u0432\u043E\u0441\u0442\u0438",
  news_page_lead: "\u0427\u0442\u043E \u043D\u043E\u0432\u043E\u0433\u043E \u043D\u0430 \u0441\u0435\u0440\u0432\u0435\u0440\u0435 \u0438 \u0432 \u043B\u0430\u0443\u043D\u0447\u0435\u0440\u0435.",
  profile_eyebrow: "\u041F\u0440\u043E\u0444\u0438\u043B\u044C",
  profile_title: "\u0418\u0433\u0440\u043E\u043A",
  profile_lead: "\u0421\u0442\u0430\u0442\u044B, \u0431\u0438\u043E, \u0442\u0435\u043C\u0430 \u043E\u0444\u043E\u0440\u043C\u043B\u0435\u043D\u0438\u044F. \u0421\u0432\u043E\u0439 \u043F\u0440\u043E\u0444\u0438\u043B\u044C \u043F\u0440\u0430\u0432\u0438\u0442\u0441\u044F \u043F\u043E\u0441\u043B\u0435 \u0432\u0445\u043E\u0434\u0430.",
  login_eyebrow: "\u0410\u043A\u043A\u0430\u0443\u043D\u0442",
  login_title: "\u0412\u0445\u043E\u0434",
  login_lead: "\u041D\u0438\u043A Minecraft: \u043B\u0430\u0442\u0438\u043D\u0438\u0446\u0430, \u0446\u0438\u0444\u0440\u044B, _.",
  register_eyebrow: "\u0410\u043A\u043A\u0430\u0443\u043D\u0442",
  register_title: "\u0420\u0435\u0433\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u044F",
  register_lead: "\u041D\u0438\u043A 3\u201316 \u0441\u0438\u043C\u0432\u043E\u043B\u043E\u0432 (A\u2013Z, 0\u20139, _). \u041F\u0430\u0440\u043E\u043B\u044C \u043E\u0442 4.",
  players_eyebrow: "\u0418\u0433\u0440\u043E\u043A\u0438",
  players_title: "\u041F\u043E\u0438\u0441\u043A",
  players_lead: "\u0412\u0432\u0435\u0434\u0438 \u043D\u0438\u043A, \u043E\u0442\u043A\u0440\u043E\u0439 \u043F\u0440\u043E\u0444\u0438\u043B\u044C.",
  rules_eyebrow: "\u041F\u0440\u0430\u0432\u0438\u043B\u0430",
  rules_title: "\u041D\u0430 \u0441\u0435\u0440\u0432\u0435\u0440\u0435",
  rules_1: "\u0427\u0438\u0442\u044B \u0438 \u0434\u044E\u043F\u044B \u0437\u0430\u043F\u0440\u0435\u0449\u0435\u043D\u044B.",
  rules_2: "\u041D\u0435 \u043C\u0435\u0448\u0430\u0439 \u043D\u0430 \u0441\u043F\u0430\u0432\u043D\u0435 \u0438 \u0432 \u0447\u0443\u0436\u0438\u0445 \u0431\u0430\u0437\u0430\u0445.",
  rules_3: "\u0411\u0435\u0437 \u043E\u0441\u043A\u043E\u0440\u0431\u043B\u0435\u043D\u0438\u0439 \u0432 \u0447\u0430\u0442\u0435.",
  rules_4: "\u0427\u0443\u0436\u0438\u0435 \u0441\u0435\u0440\u0432\u0435\u0440\u0430 \u0432 \u0447\u0430\u0442\u0435 \u043D\u0435 \u0440\u0435\u043A\u043B\u0430\u043C\u0438\u0440\u0443\u0439.",
  rules_5: "\u0410\u0434\u043C\u0438\u043D\u044B \u043C\u043E\u0433\u0443\u0442 \u043E\u0442\u043A\u0430\u0442\u0438\u0442\u044C \u0433\u0440\u0438\u0444 \u0438 \u0432\u044B\u0434\u0430\u0442\u044C \u043C\u0443\u0442/\u0431\u0430\u043D."
};
function copyMaxLen(key) {
  if (key.endsWith("_lead") || key.endsWith("_body") || key.includes("_rule_") || key.startsWith("rules_") || key.endsWith("_notice") || key === "footer_blurb") {
    return 800;
  }
  return 160;
}
__name(copyMaxLen, "copyMaxLen");
async function getSiteCopy(db) {
  await ensureSettings(db);
  const out = { ...SITE_COPY_DEFAULTS };
  for (const key of SITE_COPY_KEYS) {
    out[key] = await getSetting(db, key, SITE_COPY_DEFAULTS[key]);
  }
  return out;
}
__name(getSiteCopy, "getSiteCopy");
async function patchSiteCopy(db, patch) {
  if (!patch || typeof patch !== "object") return getSiteCopy(db);
  for (const key of SITE_COPY_KEYS) {
    if (!(key in patch)) continue;
    const val = String(patch[key] ?? "").trim();
    if (!val) continue;
    await setSetting(db, key, val.slice(0, copyMaxLen(key)));
  }
  return getSiteCopy(db);
}
__name(patchSiteCopy, "patchSiteCopy");

// api/admin/settings.js
async function onRequestGet4(context2) {
  const { request, env: env2 } = context2;
  if (!env2.DB) return bad("\u0411\u0430\u0437\u0430 \u043D\u0435 \u043F\u043E\u0434\u043A\u043B\u044E\u0447\u0435\u043D\u0430", 503);
  const admin = await requireAdmin(env2.DB, request, env2);
  if (!admin) return bad("\u041D\u0435\u0442 \u0434\u043E\u0441\u0442\u0443\u043F\u0430", 403);
  return json({
    ok: true,
    settings: {
      purchases_enabled: await purchasesEnabled(env2),
      catalog_from_db: await getSetting(env2.DB, "catalog_from_db", "0") === "1"
    },
    copy: await getSiteCopy(env2.DB)
  });
}
__name(onRequestGet4, "onRequestGet");
async function onRequestPatch4(context2) {
  const { request, env: env2 } = context2;
  if (!env2.DB) return bad("\u0411\u0430\u0437\u0430 \u043D\u0435 \u043F\u043E\u0434\u043A\u043B\u044E\u0447\u0435\u043D\u0430", 503);
  const admin = await requireAdmin(env2.DB, request, env2);
  if (!admin) return bad("\u041D\u0435\u0442 \u0434\u043E\u0441\u0442\u0443\u043F\u0430", 403);
  const body = await readJson(request);
  if (!body || typeof body !== "object") return bad("\u041D\u0443\u0436\u0435\u043D JSON");
  if ("purchases_enabled" in body) {
    await setSetting(env2.DB, "purchases_enabled", body.purchases_enabled ? "true" : "false");
  }
  if ("catalog_from_db" in body) {
    await setSetting(env2.DB, "catalog_from_db", body.catalog_from_db ? "1" : "0");
  }
  if (body.copy && typeof body.copy === "object") {
    await patchSiteCopy(env2.DB, body.copy);
  }
  return json({
    ok: true,
    settings: {
      purchases_enabled: await purchasesEnabled(env2),
      catalog_from_db: await getSetting(env2.DB, "catalog_from_db", "0") === "1"
    },
    copy: await getSiteCopy(env2.DB)
  });
}
__name(onRequestPatch4, "onRequestPatch");

// api/admin/users.js
async function onRequestGet5(context2) {
  const { request, env: env2 } = context2;
  if (!env2.DB) return bad("\u0411\u0430\u0437\u0430 \u043D\u0435 \u043F\u043E\u0434\u043A\u043B\u044E\u0447\u0435\u043D\u0430", 503);
  const admin = await requireAdmin(env2.DB, request, env2);
  if (!admin) return bad("\u041D\u0435\u0442 \u0434\u043E\u0441\u0442\u0443\u043F\u0430", 403);
  const url = new URL(request.url);
  const q = String(url.searchParams.get("q") || "").trim().slice(0, 32);
  const limit = Math.min(50, Math.max(1, Number(url.searchParams.get("limit") || 40) || 40));
  let sql = `SELECT u.id, u.nick, u.created_at,
                    COALESCE(u.is_admin, 0) AS is_admin,
                    p.privilege, p.coins, p.likes, p.fish, p.playtime_hours, p.bio
             FROM users u
             LEFT JOIN profiles p ON p.user_id = u.id`;
  const binds = [];
  if (q) {
    sql += " WHERE u.nick LIKE ? COLLATE NOCASE";
    binds.push(`%${q}%`);
  }
  sql += " ORDER BY u.created_at DESC LIMIT ?";
  binds.push(limit);
  let res;
  try {
    res = await env2.DB.prepare(sql).bind(...binds).all();
  } catch {
    sql = `SELECT u.id, u.nick, u.created_at, 0 AS is_admin,
                  p.privilege, p.coins, p.likes, p.fish, p.playtime_hours, p.bio
           FROM users u
           LEFT JOIN profiles p ON p.user_id = u.id`;
    const binds2 = [];
    if (q) {
      sql += " WHERE u.nick LIKE ? COLLATE NOCASE";
      binds2.push(`%${q}%`);
    }
    sql += " ORDER BY u.created_at DESC LIMIT ?";
    binds2.push(limit);
    res = await env2.DB.prepare(sql).bind(...binds2).all();
  }
  const users = (res.results || []).map((row) => ({
    id: row.id,
    nick: row.nick,
    created_at: row.created_at,
    is_admin: Number(row.is_admin) === 1,
    privilege: row.privilege || "\u0418\u0433\u0440\u043E\u043A",
    coins: row.coins || 0,
    likes: row.likes || 0,
    fish: row.fish || 0,
    playtime_hours: row.playtime_hours || 0,
    bio: row.bio || ""
  }));
  return json({ ok: true, users });
}
__name(onRequestGet5, "onRequestGet");

// api/launcher/ensure-nick.js
async function onRequestPost4() {
  return bad("ensure-nick \u043E\u0442\u043A\u043B\u044E\u0447\u0451\u043D \u2014 \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u0443\u0439\u0441\u044F \u043D\u0430 \u0441\u0430\u0439\u0442\u0435", 410);
}
__name(onRequestPost4, "onRequestPost");

// api/launcher/session.js
function launcherOnly(request) {
  return request.headers.get("x-aquatech-launcher") === "1";
}
__name(launcherOnly, "launcherOnly");
async function onRequestGet6(context2) {
  const { request, env: env2 } = context2;
  if (!launcherOnly(request)) return bad("Forbidden", 403);
  if (!env2.DB) return bad("\u0411\u0430\u0437\u0430 \u043D\u0435 \u043F\u043E\u0434\u043A\u043B\u044E\u0447\u0435\u043D\u0430", 503);
  const user = await requireUser(env2.DB, request);
  if (!user) return bad("\u041D\u0435 \u0430\u0432\u0442\u043E\u0440\u0438\u0437\u043E\u0432\u0430\u043D", 401);
  const sid = getSessionId(request);
  if (!sid) return bad("\u0421\u0435\u0441\u0441\u0438\u044F \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D\u0430", 401);
  return json({ ok: true, session: sid, user: { nick: user.nick } });
}
__name(onRequestGet6, "onRequestGet");
async function onRequestPost5(context2) {
  const { request, env: env2 } = context2;
  if (!launcherOnly(request)) return bad("Forbidden", 403);
  if (!env2.DB) return bad("\u0411\u0430\u0437\u0430 \u043D\u0435 \u043F\u043E\u0434\u043A\u043B\u044E\u0447\u0435\u043D\u0430", 503);
  const body = await readJson(request);
  const sid = String(body?.session || "").trim();
  if (!sid) return bad("\u041D\u0435\u0442 \u0441\u0435\u0441\u0441\u0438\u0438", 401);
  const row = await env2.DB.prepare(
    `SELECT u.nick FROM sessions s
     JOIN users u ON u.id = s.user_id
     WHERE s.id = ? AND datetime(s.expires_at) > datetime('now')`
  ).bind(sid).first();
  if (!row) return bad("\u0421\u0435\u0441\u0441\u0438\u044F \u0438\u0441\u0442\u0435\u043A\u043B\u0430", 401);
  return json({ ok: true, session: sid, user: { nick: row.nick } });
}
__name(onRequestPost5, "onRequestPost");

// api/launcher/verify-token.js
async function onRequestPost6(context2) {
  const { request, env: env2 } = context2;
  if (request.headers.get("x-aquatech-launcher") !== "1") {
    return bad("Forbidden", 403);
  }
  if (!env2.DB) return bad("Database not connected", 503);
  const body = await readJson(request);
  const sid = String(body?.session || "").trim();
  const nick = String(body?.nick || "").trim();
  if (!sid || !nick) return bad("Missing session or nick", 400);
  const row = await env2.DB.prepare(
    `SELECT u.nick, coalesce(p.coins, 0) AS balance, coalesce(p.privilege, 'player') AS rank_id
     FROM sessions s
     JOIN users u ON u.id = s.user_id
     LEFT JOIN profiles p ON p.user_id = u.id
     WHERE s.id = ?
       AND lower(u.nick) = lower(?)
       AND datetime(s.expires_at) > datetime('now')`
  ).bind(sid, nick).first();
  if (!row) return bad("Session invalid or expired", 401);
  return json({ ok: true, nick: row.nick, balance: Number(row.balance) || 0, rank_id: String(row.rank_id || "player") });
}
__name(onRequestPost6, "onRequestPost");

// api/sync/player.js
var DEFAULT_SYNC_KEY = "aquatech_internal_sync_key_2026";
async function onRequestPost7(context2) {
  const { request, env: env2 } = context2;
  if (!env2.DB) return bad("\u0411\u0430\u0437\u0430 \u043D\u0435 \u043F\u043E\u0434\u043A\u043B\u044E\u0447\u0435\u043D\u0430 (D1)", 503);
  const serverKey = request.headers.get("X-AquaTech-Server-Key") || "";
  const expectedKey = env2.SERVER_SYNC_KEY || DEFAULT_SYNC_KEY;
  if (serverKey !== expectedKey) {
    return bad("\u041D\u0435\u0432\u0435\u0440\u043D\u044B\u0439 \u043A\u043B\u044E\u0447 \u0441\u0435\u0440\u0432\u0435\u0440\u0430", 403);
  }
  const body = await readJson(request);
  if (!body || !body.nick) return bad("\u0423\u043A\u0430\u0436\u0438\u0442\u0435 \u043D\u0438\u043A \u0438\u0433\u0440\u043E\u043A\u0430");
  const nick = String(body.nick).trim();
  const coins = Math.max(0, Math.floor(Number(body.coins || 0)));
  const fish = Math.max(0, Math.floor(Number(body.fish || 0)));
  const playtimeHours = Math.max(0, Math.floor(Number(body.playtime_hours || 0)));
  const privilege = String(body.privilege || "").slice(0, 32);
  const questsDone = Math.max(0, Math.floor(Number(body.quests_done || 0)));
  let user = await env2.DB.prepare(
    "SELECT id FROM users WHERE nick = ? COLLATE NOCASE"
  ).bind(nick).first();
  if (!user) {
    const res = await env2.DB.prepare(
      "INSERT INTO users (nick, password_hash, password_salt) VALUES (?, 'IN_GAME_UNREGISTERED', '')"
    ).bind(nick).run();
    const userId = res.meta.last_row_id;
    await env2.DB.prepare(
      `INSERT INTO profiles (user_id, bio, theme, privilege, coins, fish, playtime_hours, quests_done)
       VALUES (?, '\u0418\u0433\u0440\u043E\u043A \u0441\u0435\u0440\u0432\u0435\u0440\u0430 AquaTech.', 'ocean', ?, ?, ?, ?, ?)`
    ).bind(
      userId,
      privilege || "\u0418\u0433\u0440\u043E\u043A",
      coins,
      fish,
      playtimeHours,
      questsDone
    ).run();
  } else {
    let updates = [];
    let binds = [];
    if (body.coins !== void 0) {
      updates.push("coins = ?");
      binds.push(coins);
    }
    if (body.fish !== void 0) {
      updates.push("fish = ?");
      binds.push(fish);
    }
    if (body.playtime_hours !== void 0) {
      updates.push("playtime_hours = ?");
      binds.push(playtimeHours);
    }
    if (body.privilege !== void 0 && privilege) {
      updates.push("privilege = ?");
      binds.push(privilege);
    }
    if (body.quests_done !== void 0) {
      updates.push("quests_done = ?");
      binds.push(questsDone);
    }
    if (updates.length > 0) {
      updates.push("updated_at = strftime('%Y-%m-%dT%H:%M:%fZ', 'now')");
      binds.push(user.id);
      await env2.DB.prepare(
        `UPDATE profiles SET ${updates.join(", ")} WHERE user_id = ?`
      ).bind(...binds).run();
    }
  }
  return json({ ok: true, synced: true, nick });
}
__name(onRequestPost7, "onRequestPost");

// _lib/profile.js
function computePlayerBadges(row) {
  const list = [];
  let manual = [];
  try {
    manual = JSON.parse(row.badges_json || "[]");
  } catch {
    manual = [];
  }
  for (const b of manual) {
    if (b && typeof b === "string") {
      list.push({ title: b, rarity: "special", desc: "\u041E\u0441\u043E\u0431\u0430\u044F \u043D\u0430\u0433\u0440\u0430\u0434\u0430" });
    } else if (b && b.title) {
      list.push({
        title: b.title,
        rarity: b.rarity || "special",
        desc: b.desc || "\u041E\u0441\u043E\u0431\u044B\u0439 \u0442\u0438\u0442\u0443\u043B"
      });
    }
  }
  const fish = Number(row.fish || 0);
  if (fish >= 1e3) {
    list.push({ title: "\u041B\u0435\u0433\u0435\u043D\u0434\u0430 \u043E\u043A\u0435\u0430\u043D\u0430", rarity: "legendary", desc: "\u041F\u043E\u0439\u043C\u0430\u043D\u043E \u0431\u043E\u043B\u0435\u0435 1 000 \u0440\u044B\u0431" });
  } else if (fish >= 500) {
    list.push({ title: "\u041E\u0445\u043E\u0442\u043D\u0438\u043A \u0411\u0435\u0437\u0434\u043D\u044B", rarity: "epic", desc: "\u041F\u043E\u0439\u043C\u0430\u043D\u043E \u0431\u043E\u043B\u0435\u0435 500 \u0440\u044B\u0431" });
  } else if (fish >= 250) {
    list.push({ title: "\u041C\u0430\u0441\u0442\u0435\u0440 \u043A\u0430\u0442\u0443\u0448\u043A\u0438", rarity: "rare", desc: "\u041F\u043E\u0439\u043C\u0430\u043D\u043E \u0431\u043E\u043B\u0435\u0435 250 \u0440\u044B\u0431" });
  } else if (fish >= 100) {
    list.push({ title: "\u041E\u043F\u044B\u0442\u043D\u044B\u0439 \u0443\u0434\u0438\u043B\u044C\u0449\u0438\u043A", rarity: "rare", desc: "\u041F\u043E\u0439\u043C\u0430\u043D\u043E \u0431\u043E\u043B\u0435\u0435 100 \u0440\u044B\u0431" });
  } else if (fish >= 25) {
    list.push({ title: "\u0420\u044B\u0431\u043E\u043B\u043E\u0432-\u043B\u044E\u0431\u0438\u0442\u0435\u043B\u044C", rarity: "common", desc: "\u041F\u043E\u0439\u043C\u0430\u043D\u043E \u0431\u043E\u043B\u0435\u0435 25 \u0440\u044B\u0431" });
  } else {
    list.push({ title: "\u041D\u043E\u0432\u0438\u0447\u043E\u043A \u0433\u043B\u0443\u0431\u0438\u043D", rarity: "common", desc: "\u041F\u0435\u0440\u0432\u044B\u0435 \u0448\u0430\u0433\u0438 \u0432 \u0440\u044B\u0431\u0430\u043B\u043A\u0435" });
  }
  const coins = Number(row.coins || 0);
  if (coins >= 5e5) {
    list.push({ title: "\u041E\u043B\u0438\u0433\u0430\u0440\u0445 \u0433\u043B\u0443\u0431\u0438\u043D", rarity: "legendary", desc: "\u0411\u0430\u043B\u0430\u043D\u0441 \u0431\u043E\u043B\u0435\u0435 500 000 \xA4" });
  } else if (coins >= 1e5) {
    list.push({ title: "\u041E\u043A\u0435\u0430\u043D\u0441\u043A\u0438\u0439 \u043C\u0430\u0433\u043D\u0430\u0442", rarity: "epic", desc: "\u0411\u0430\u043B\u0430\u043D\u0441 \u0431\u043E\u043B\u0435\u0435 100 000 \xA4" });
  } else if (coins >= 5e4) {
    list.push({ title: "\u0421\u043E\u0441\u0442\u043E\u044F\u0442\u0435\u043B\u044C\u043D\u044B\u0439", rarity: "rare", desc: "\u0411\u0430\u043B\u0430\u043D\u0441 \u0431\u043E\u043B\u0435\u0435 50 000 \xA4" });
  } else if (coins >= 1e4) {
    list.push({ title: "\u041F\u0435\u0440\u0432\u044B\u0439 \u043A\u0430\u043F\u0438\u0442\u0430\u043B", rarity: "common", desc: "\u0411\u0430\u043B\u0430\u043D\u0441 \u0431\u043E\u043B\u0435\u0435 10 000 \xA4" });
  }
  const hours = Number(row.playtime_hours || 0);
  if (hours >= 100) {
    list.push({ title: "\u0425\u0440\u0430\u043D\u0438\u0442\u0435\u043B\u044C \u043E\u043A\u0435\u0430\u043D\u0430", rarity: "legendary", desc: "\u0411\u043E\u043B\u0435\u0435 100 \u0447\u0430\u0441\u043E\u0432 \u043D\u0430 \u0441\u0435\u0440\u0432\u0435\u0440\u0435" });
  } else if (hours >= 50) {
    list.push({ title: "\u0412\u0435\u0442\u0435\u0440\u0430\u043D AquaTech", rarity: "epic", desc: "\u0411\u043E\u043B\u0435\u0435 50 \u0447\u0430\u0441\u043E\u0432 \u043D\u0430 \u0441\u0435\u0440\u0432\u0435\u0440\u0435" });
  } else if (hours >= 20) {
    list.push({ title: "\u0411\u044B\u0432\u0430\u043B\u044B\u0439 \u043C\u043E\u0440\u0435\u043F\u043B\u0430\u0432\u0430\u0442\u0435\u043B\u044C", rarity: "rare", desc: "\u0411\u043E\u043B\u0435\u0435 20 \u0447\u0430\u0441\u043E\u0432 \u043D\u0430 \u0441\u0435\u0440\u0432\u0435\u0440\u0435" });
  } else if (hours >= 5) {
    list.push({ title: "\u0416\u0438\u0442\u0435\u043B\u044C \u043F\u043B\u043E\u0442\u0430", rarity: "common", desc: "\u0411\u043E\u043B\u0435\u0435 5 \u0447\u0430\u0441\u043E\u0432 \u043D\u0430 \u0441\u0435\u0440\u0432\u0435\u0440\u0435" });
  }
  const likes = Number(row.likes || 0);
  if (likes >= 50) {
    list.push({ title: "\u0417\u0432\u0435\u0437\u0434\u0430 \u0441\u043E\u043E\u0431\u0449\u0435\u0441\u0442\u0432\u0430", rarity: "legendary", desc: "\u0411\u043E\u043B\u0435\u0435 50 \u043F\u043E\u0445\u0432\u0430\u043B \u043E\u0442 \u0438\u0433\u0440\u043E\u043A\u043E\u0432" });
  } else if (likes >= 15) {
    list.push({ title: "\u041B\u044E\u0431\u0438\u043C\u0435\u0446 \u043E\u043A\u0435\u0430\u043D\u0430", rarity: "epic", desc: "\u0411\u043E\u043B\u0435\u0435 15 \u043F\u043E\u0445\u0432\u0430\u043B \u043E\u0442 \u0438\u0433\u0440\u043E\u043A\u043E\u0432" });
  } else if (likes >= 5) {
    list.push({ title: "\u0417\u0430\u043C\u0435\u0442\u043D\u044B\u0439 \u0438\u0433\u0440\u043E\u043A", rarity: "rare", desc: "\u0411\u043E\u043B\u0435\u0435 5 \u043F\u043E\u0445\u0432\u0430\u043B \u043E\u0442 \u0438\u0433\u0440\u043E\u043A\u043E\u0432" });
  }
  const priv = String(row.privilege || "").trim();
  if (priv && priv !== "\u0418\u0433\u0440\u043E\u043A") {
    const isStaff = ["\u0421\u043E\u0437\u0434\u0430\u0442\u0435\u043B\u044C", "Owner", "Admin", "\u0420\u0430\u0437\u0440\u0430\u0431\u043E\u0442\u0447\u0438\u043A", "Developer", "\u0423\u043F\u0440\u0430\u0432\u043B\u044F\u044E\u0449\u0438\u0439", "Manager"].includes(priv);
    list.push({
      title: priv,
      rarity: isStaff ? "legendary" : "epic",
      desc: `\u041F\u0440\u0438\u0432\u0438\u043B\u0435\u0433\u0438\u044F ${priv}`
    });
  }
  const seen = /* @__PURE__ */ new Set();
  const deduped = [];
  for (const item of list) {
    if (!seen.has(item.title.toLowerCase())) {
      seen.add(item.title.toLowerCase());
      deduped.push(item);
    }
  }
  return deduped;
}
__name(computePlayerBadges, "computePlayerBadges");
function mapProfile(row) {
  if (!row) return null;
  const badges = computePlayerBadges(row);
  let learnedSkills = ["origin"];
  try {
    if (row.learned_skills_json) {
      learnedSkills = JSON.parse(row.learned_skills_json);
    }
  } catch {
    learnedSkills = ["origin"];
  }
  return {
    nick: row.nick,
    bio: row.bio || "\u0418\u0441\u0441\u043B\u0435\u0434\u043E\u0432\u0430\u0442\u0435\u043B\u044C \u0433\u043B\u0443\u0431\u0438\u043D AquaTech.",
    theme: row.theme || "ocean",
    status_message: row.status_message || "",
    fav_rod: row.fav_rod || "",
    social_tg: row.social_tg || "",
    social_vk: row.social_vk || "",
    social_discord: row.social_discord || "",
    privilege: row.privilege || "\u0418\u0433\u0440\u043E\u043A",
    coins: row.coins ?? 0,
    likes: row.likes ?? 0,
    fish: row.fish ?? 0,
    has_liked: Boolean(row.has_liked),
    skill_points: row.skill_points ?? 0,
    learned_skills: learnedSkills,
    quests_done: row.quests_done ?? 0,
    quests_total: row.quests_total || 25,
    leaderboard_rank: row.leaderboard_rank || 1,
    playtime: `${row.playtime_hours ?? 0} \u0447`,
    playtime_hours: row.playtime_hours ?? 0,
    views: row.views ?? 0,
    badges,
    updated_at: row.updated_at
  };
}
__name(mapProfile, "mapProfile");
async function fetchProfileByNick(db, nick, currentUserId = null) {
  if (currentUserId) {
    return db.prepare(
      `SELECT u.id AS user_id, u.nick, p.bio, p.theme, p.status_message, p.fav_rod,
                p.social_tg, p.social_vk, p.social_discord,
                p.privilege, p.coins, p.likes, p.fish,
                p.skill_points, p.learned_skills_json, p.quests_done, p.quests_total, p.leaderboard_rank,
                p.playtime_hours, p.views, p.badges_json, p.updated_at,
                (SELECT 1 FROM profile_likes WHERE from_user_id = ? AND to_user_id = u.id LIMIT 1) AS has_liked
         FROM users u
         JOIN profiles p ON p.user_id = u.id
         WHERE u.nick = ? COLLATE NOCASE`
    ).bind(currentUserId, nick).first();
  }
  return db.prepare(
    `SELECT u.id AS user_id, u.nick, p.bio, p.theme, p.status_message, p.fav_rod,
              p.social_tg, p.social_vk, p.social_discord,
              p.privilege, p.coins, p.likes, p.fish,
              p.skill_points, p.learned_skills_json, p.quests_done, p.quests_total, p.leaderboard_rank,
              p.playtime_hours, p.views, p.badges_json, p.updated_at,
              0 AS has_liked
       FROM users u
       JOIN profiles p ON p.user_id = u.id
       WHERE u.nick = ? COLLATE NOCASE`
  ).bind(nick).first();
}
__name(fetchProfileByNick, "fetchProfileByNick");
async function bumpViews(db, nick) {
  await db.prepare(
    `UPDATE profiles SET views = views + 1, updated_at = strftime('%Y-%m-%dT%H:%M:%fZ', 'now')
       WHERE user_id = (SELECT id FROM users WHERE nick = ? COLLATE NOCASE)`
  ).bind(nick).run();
}
__name(bumpViews, "bumpViews");

// api/profiles/[nick].js
var VALID_THEMES = [
  "ocean",
  "deep",
  "storm",
  "abyss",
  "magma",
  "celestial",
  "cyber",
  "aurora"
];
async function onRequestGet7(context2) {
  const { request, env: env2, params } = context2;
  if (!env2.DB) return bad("\u0411\u0430\u0437\u0430 \u043D\u0435 \u043F\u043E\u0434\u043A\u043B\u044E\u0447\u0435\u043D\u0430 (D1)", 503);
  const nick = String(params.nick || "").trim();
  if (!nick) return bad("\u041D\u0438\u043A \u043D\u0435 \u0443\u043A\u0430\u0437\u0430\u043D");
  const currentUser = await requireUser(env2.DB, request);
  const row = await fetchProfileByNick(env2.DB, nick, currentUser?.id);
  if (!row) return bad("\u0418\u0433\u0440\u043E\u043A \u043D\u0435 \u043D\u0430\u0439\u0434\u0435\u043D", 404);
  await bumpViews(env2.DB, nick);
  const fresh = await fetchProfileByNick(env2.DB, nick, currentUser?.id);
  return json({ ok: true, profile: mapProfile(fresh) });
}
__name(onRequestGet7, "onRequestGet");
async function onRequestPatch5(context2) {
  const { request, env: env2, params } = context2;
  if (!env2.DB) return bad("\u0411\u0430\u0437\u0430 \u043D\u0435 \u043F\u043E\u0434\u043A\u043B\u044E\u0447\u0435\u043D\u0430 (D1)", 503);
  const user = await requireUser(env2.DB, request);
  if (!user) return bad("\u041D\u0435 \u0430\u0432\u0442\u043E\u0440\u0438\u0437\u043E\u0432\u0430\u043D", 401);
  const nick = String(params.nick || "").trim();
  if (user.nick.toLowerCase() !== nick.toLowerCase()) {
    return bad("\u041C\u043E\u0436\u043D\u043E \u0440\u0435\u0434\u0430\u043A\u0442\u0438\u0440\u043E\u0432\u0430\u0442\u044C \u0442\u043E\u043B\u044C\u043A\u043E \u0441\u0432\u043E\u0439 \u043F\u0440\u043E\u0444\u0438\u043B\u044C", 403);
  }
  const body = await readJson(request);
  if (!body) return bad("\u041D\u0435\u043A\u043E\u0440\u0440\u0435\u043A\u0442\u043D\u044B\u0439 JSON");
  const bio = String(body.bio ?? "").slice(0, 300);
  const theme = VALID_THEMES.includes(body.theme) ? body.theme : "ocean";
  const status_message = String(body.status_message ?? "").slice(0, 80);
  const fav_rod = String(body.fav_rod ?? "").slice(0, 50);
  const social_tg = String(body.social_tg ?? "").slice(0, 60);
  const social_vk = String(body.social_vk ?? "").slice(0, 60);
  const social_discord = String(body.social_discord ?? "").slice(0, 60);
  await env2.DB.prepare(
    `UPDATE profiles
     SET bio = ?, theme = ?, status_message = ?, fav_rod = ?,
         social_tg = ?, social_vk = ?, social_discord = ?,
         updated_at = strftime('%Y-%m-%dT%H:%M:%fZ', 'now')
     WHERE user_id = ?`
  ).bind(
    bio || "\u0418\u0441\u0441\u043B\u0435\u0434\u043E\u0432\u0430\u0442\u0435\u043B\u044C \u0433\u043B\u0443\u0431\u0438\u043D AquaTech.",
    theme,
    status_message,
    fav_rod,
    social_tg,
    social_vk,
    social_discord,
    user.id
  ).run();
  const row = await fetchProfileByNick(env2.DB, user.nick, user.id);
  return json({ ok: true, profile: mapProfile(row) });
}
__name(onRequestPatch5, "onRequestPatch");

// api/catalog.js
var COPY = {
  vip: {
    description: "\u041F\u0440\u0435\u0444\u0438\u043A\u0441, \u0446\u0432\u0435\u0442\u043D\u043E\u0439 \u043D\u0438\u043A, +1 \u0434\u043E\u043C. \u041A\u0443\u043F\u0438\u0442\u044C \u043D\u0430 \u0441\u0430\u0439\u0442\u0435 \u043F\u043E\u043A\u0430 \u043D\u0435\u043B\u044C\u0437\u044F.",
    perks: ["\u041F\u0440\u0435\u0444\u0438\u043A\u0441 VIP \u0432 \u0447\u0430\u0442\u0435", "+1 \u0434\u043E\u043C /sethome", "\u0426\u0432\u0435\u0442\u043D\u043E\u0439 \u043D\u0438\u043A", "\u041F\u0440\u0438\u043E\u0440\u0438\u0442\u0435\u0442 \u0432 \u043E\u0447\u0435\u0440\u0435\u0434\u0438"]
  },
  premium: {
    description: "\u0412\u0441\u0451 \u0438\u0437 VIP, \u043A\u0435\u0439\u0441 \u0432 \u0434\u0435\u043D\u044C \u043D\u0430 \u0441\u0435\u0440\u0432\u0435\u0440\u0435, \u043F\u0440\u0438\u043E\u0440\u0438\u0442\u0435\u0442 \u0432\u0445\u043E\u0434\u0430.",
    perks: ["\u0412\u0441\u0451 \u0438\u0437 VIP", "\u041A\u0435\u0439\u0441 \u0432 \u0434\u0435\u043D\u044C (\u0432 \u0438\u0433\u0440\u0435)", "\u041F\u0440\u0438\u043E\u0440\u0438\u0442\u0435\u0442 \u0432\u0445\u043E\u0434\u0430", "\u0414\u043E\u043F. \u0441\u043B\u043E\u0442 \u0432\u0430\u0440\u043F\u0430"]
  },
  deluxe: {
    description: "\u0411\u043E\u043D\u0443\u0441 \u043A \u0443\u043B\u043E\u0432\u0443 \u0438 \u0440\u0430\u043C\u043A\u0430 \u043F\u0440\u043E\u0444\u0438\u043B\u044F. \u041E\u043F\u043B\u0430\u0442\u0430 \u043D\u0430 \u0441\u0430\u0439\u0442\u0435 \u0432\u044B\u043A\u043B\u044E\u0447\u0435\u043D\u0430.",
    perks: ["\u0412\u0441\u0451 \u0438\u0437 Premium", "\u0420\u0430\u043C\u043A\u0430 \u043F\u0440\u043E\u0444\u0438\u043B\u044F", "\u0411\u043E\u043D\u0443\u0441 \u043A \u0443\u043B\u043E\u0432\u0443", "\u0411\u0435\u0439\u0434\u0436 Deluxe"]
  },
  ultimate: {
    description: "\u041C\u0430\u043A\u0441\u0438\u043C\u0443\u043C \u043F\u0440\u0438\u0432\u0438\u043B\u0435\u0433\u0438\u0439 \u043D\u0430 \u0441\u0435\u0440\u0432\u0435\u0440\u0435. \u041E\u043F\u043B\u0430\u0442\u0430 \u043D\u0430 \u0441\u0430\u0439\u0442\u0435 \u043F\u043E\u0437\u0436\u0435.",
    perks: ["\u0412\u0441\u0451 \u0438\u0437 Deluxe", "\u0411\u0435\u0439\u0434\u0436 Ultimate", "\u041C\u0430\u043A\u0441\u0438\u043C\u0443\u043C \u0434\u043E\u043C\u043E\u0432", "\u041F\u0440\u0438\u043E\u0440\u0438\u0442\u0435\u0442 \u0432 \u043F\u043E\u0434\u0434\u0435\u0440\u0436\u043A\u0435"]
  },
  ocean: {
    description: "\u041C\u043E\u043D\u0435\u0442\u044B \u0438 \u0440\u0430\u0441\u0445\u043E\u0434\u043D\u0438\u043A\u0438. \u041E\u0442\u043A\u0440\u044B\u0432\u0430\u0435\u0442\u0441\u044F \u0432 \u0438\u0433\u0440\u0435 (F4).",
    perks: ["AquaCoins", "\u0420\u0430\u0441\u0445\u043E\u0434\u043D\u0438\u043A\u0438", "\u041C\u0435\u043B\u043A\u0438\u0439 \u0431\u0443\u0441\u0442"]
  },
  fisher: {
    description: "\u041B\u0443\u0442 \u043F\u043E\u0434 StarCatcher. \u0420\u0443\u043B\u0435\u0442\u043A\u0438 \u043D\u0430 \u0441\u0430\u0439\u0442\u0435 \u043D\u0435\u0442.",
    perks: ["\u0420\u0435\u0441\u0443\u0440\u0441\u044B \u0443\u043B\u043E\u0432\u0430", "\u0411\u0443\u0441\u0442 \u0443\u0434\u043E\u0447\u043A\u0438", "\u041C\u043E\u043D\u0435\u0442\u044B"]
  },
  depth: {
    description: "\u0420\u0435\u0434\u043A\u0430\u044F \u043A\u043E\u0441\u043C\u0435\u0442\u0438\u043A\u0430 \u0438 \u043F\u0440\u043E\u0431\u043D\u044B\u0435 \u043F\u0440\u0438\u0432\u0438\u043B\u0435\u0433\u0438\u0438. \u0422\u043E\u043B\u044C\u043A\u043E \u0441\u0435\u0440\u0432\u0435\u0440.",
    perks: ["\u0420\u0430\u043C\u043A\u0430 \u043F\u0440\u043E\u0444\u0438\u043B\u044F", "\u041F\u0440\u043E\u0431\u043D\u0430\u044F \u043F\u0440\u0438\u0432\u0438\u043B\u0435\u0433\u0438\u044F", "\u041A\u0440\u0443\u043F\u043D\u044B\u0439 \u0437\u0430\u043F\u0430\u0441 \u043C\u043E\u043D\u0435\u0442"]
  }
};
async function onRequestGet8(context2) {
  const { request, env: env2 } = context2;
  if (!env2.DB) return bad("\u0411\u0430\u0437\u0430 \u043D\u0435 \u043F\u043E\u0434\u043A\u043B\u044E\u0447\u0435\u043D\u0430", 503);
  const url = new URL(request.url);
  const kind = url.searchParams.get("kind");
  const fromDb = await getSetting(env2.DB, "catalog_from_db", "0") === "1";
  let sql = `SELECT id, kind, slug, title, price_rub, description, perks_json, enabled, sort_order
             FROM catalog_items WHERE enabled = 1`;
  const binds = [];
  if (kind === "store" || kind === "case") {
    sql += " AND kind = ?";
    binds.push(kind);
  }
  sql += " ORDER BY sort_order ASC, id ASC";
  const stmt = env2.DB.prepare(sql);
  const res = await (binds.length ? stmt.bind(...binds) : stmt).all();
  const items = (res.results || []).map((row) => {
    let perks = [];
    try {
      perks = JSON.parse(row.perks_json || "[]");
    } catch {
      perks = [];
    }
    const override = fromDb ? null : COPY[row.slug];
    return {
      id: row.id,
      kind: row.kind,
      slug: row.slug,
      title: row.title,
      price_rub: row.price_rub,
      description: override?.description || row.description,
      perks: override?.perks || perks
    };
  });
  return json({
    ok: true,
    purchases_enabled: await purchasesEnabled(env2),
    items
  });
}
__name(onRequestGet8, "onRequestGet");

// api/login.js
async function onRequestPost8(context2) {
  const { request, env: env2 } = context2;
  if (!env2.DB) return bad("\u0411\u0430\u0437\u0430 \u043D\u0435 \u043F\u043E\u0434\u043A\u043B\u044E\u0447\u0435\u043D\u0430 (D1)", 503);
  const body = await readJson(request);
  if (!body) return bad("\u041D\u0435\u043A\u043E\u0440\u0440\u0435\u043A\u0442\u043D\u044B\u0439 JSON");
  const nick = normalizeNick(body.nick);
  const password = String(body.password || "");
  if (!nickOk(nick) || password.length < 1) return bad("\u041D\u0435\u0432\u0435\u0440\u043D\u044B\u0439 \u043B\u043E\u0433\u0438\u043D \u0438\u043B\u0438 \u043F\u0430\u0440\u043E\u043B\u044C", 401);
  const user = await env2.DB.prepare(
    "SELECT id, nick, password_hash, password_salt FROM users WHERE nick = ? COLLATE NOCASE"
  ).bind(nick).first();
  if (!user) return bad("\u041D\u0435\u0432\u0435\u0440\u043D\u044B\u0439 \u043B\u043E\u0433\u0438\u043D \u0438\u043B\u0438 \u043F\u0430\u0440\u043E\u043B\u044C", 401);
  const ok = await verifyPassword(password, user.password_hash, user.password_salt);
  if (!ok) return bad("\u041D\u0435\u0432\u0435\u0440\u043D\u044B\u0439 \u043B\u043E\u0433\u0438\u043D \u0438\u043B\u0438 \u043F\u0430\u0440\u043E\u043B\u044C", 401);
  const sid = newSessionId();
  await env2.DB.prepare("INSERT INTO sessions (id, user_id, expires_at) VALUES (?, ?, ?)").bind(sid, user.id, sessionExpiryIso()).run();
  return json(
    {
      ok: true,
      user: { nick: user.nick },
      ...request.headers.get("x-aquatech-launcher") === "1" ? { session: sid } : {}
    },
    200,
    { "set-cookie": sessionCookie(sid) }
  );
}
__name(onRequestPost8, "onRequestPost");

// api/logout.js
async function onRequestPost9(context2) {
  const { request, env: env2 } = context2;
  const sid = getSessionId(request);
  if (sid && env2.DB) {
    await env2.DB.prepare("DELETE FROM sessions WHERE id = ?").bind(sid).run();
  }
  return json({ ok: true }, 200, { "set-cookie": clearSessionCookie() });
}
__name(onRequestPost9, "onRequestPost");

// api/me.js
async function onRequestGet9(context2) {
  const { request, env: env2 } = context2;
  if (!env2.DB) return bad("\u0411\u0430\u0437\u0430 \u043D\u0435 \u043F\u043E\u0434\u043A\u043B\u044E\u0447\u0435\u043D\u0430", 503);
  const user = await requireUser(env2.DB, request);
  if (!user) return bad("\u041D\u0435 \u0430\u0432\u0442\u043E\u0440\u0438\u0437\u043E\u0432\u0430\u043D", 401);
  const row = await fetchProfileByNick(env2.DB, user.nick);
  const is_admin = await userIsAdmin(env2.DB, user.nick, env2);
  return json({
    ok: true,
    user: { nick: user.nick, is_admin },
    profile: mapProfile(row)
  });
}
__name(onRequestGet9, "onRequestGet");

// api/news.js
async function onRequestGet10(context2) {
  const { env: env2 } = context2;
  if (!env2.DB) return bad("\u0411\u0430\u0437\u0430 \u043D\u0435 \u043F\u043E\u0434\u043A\u043B\u044E\u0447\u0435\u043D\u0430", 503);
  try {
    const news = await listNews(env2.DB, { publishedOnly: true, limit: 40 });
    return json({ ok: true, news });
  } catch (err) {
    return bad(err?.message || "\u041D\u0435 \u0443\u0434\u0430\u043B\u043E\u0441\u044C \u0437\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044C \u043D\u043E\u0432\u043E\u0441\u0442\u0438", 500);
  }
}
__name(onRequestGet10, "onRequestGet");

// api/players.js
async function onRequestGet11(context2) {
  const { request, env: env2 } = context2;
  if (!env2.DB) return bad("\u0411\u0430\u0437\u0430 \u043D\u0435 \u043F\u043E\u0434\u043A\u043B\u044E\u0447\u0435\u043D\u0430 (D1)", 503);
  const url = new URL(request.url);
  const q = (url.searchParams.get("q") || "").trim();
  const sort = url.searchParams.get("sort") || "likes";
  const limit = Math.min(50, Math.max(1, Number(url.searchParams.get("limit") || 30)));
  const order = sort === "fish" ? "p.fish DESC" : sort === "coins" ? "p.coins DESC" : sort === "playtime" ? "p.playtime_hours DESC" : "p.likes DESC";
  let sql = `
    SELECT u.nick, p.bio, p.theme, p.privilege, p.coins, p.likes, p.fish,
           p.playtime_hours, p.views, p.badges_json, p.updated_at
    FROM users u
    JOIN profiles p ON p.user_id = u.id
  `;
  const binds = [];
  if (q) {
    sql += " WHERE u.nick LIKE ? COLLATE NOCASE";
    binds.push(`%${q.replace(/[%_]/g, "")}%`);
  }
  sql += ` ORDER BY ${order} LIMIT ?`;
  binds.push(limit);
  const stmt = env2.DB.prepare(sql);
  const res = await (binds.length ? stmt.bind(...binds) : stmt).all();
  const players = (res.results || []).map(mapProfile);
  return json({ ok: true, players });
}
__name(onRequestGet11, "onRequestGet");

// api/purchase.js
async function onRequestPost10() {
  return purchasesDisabled();
}
__name(onRequestPost10, "onRequestPost");
async function onRequestGet12() {
  return purchasesDisabled();
}
__name(onRequestGet12, "onRequestGet");

// api/register.js
async function onRequestPost11(context2) {
  const { request, env: env2 } = context2;
  if (!env2.DB) return bad("\u0411\u0430\u0437\u0430 \u043D\u0435 \u043F\u043E\u0434\u043A\u043B\u044E\u0447\u0435\u043D\u0430 (D1)", 503);
  const body = await readJson(request);
  if (!body) return bad("\u041D\u0435\u043A\u043E\u0440\u0440\u0435\u043A\u0442\u043D\u044B\u0439 JSON");
  const nick = normalizeNick(body.nick);
  const password = String(body.password || "");
  if (!nickOk(nick)) return bad("\u041D\u0438\u043A: 3\u201316 \u0441\u0438\u043C\u0432\u043E\u043B\u043E\u0432 (\u043B\u0430\u0442\u0438\u043D\u0438\u0446\u0430, \u0446\u0438\u0444\u0440\u044B, _)");
  if (password.length < 4) return bad("\u041F\u0430\u0440\u043E\u043B\u044C \u043E\u0442 4 \u0441\u0438\u043C\u0432\u043E\u043B\u043E\u0432");
  const exists = await env2.DB.prepare("SELECT id FROM users WHERE nick = ? COLLATE NOCASE").bind(nick).first();
  if (exists) return bad("\u041D\u0438\u043A \u0443\u0436\u0435 \u0437\u0430\u043D\u044F\u0442", 409);
  const { hash, salt } = await hashPassword(password);
  const created = await env2.DB.prepare(
    "INSERT INTO users (nick, password_hash, password_salt) VALUES (?, ?, ?) RETURNING id"
  ).bind(nick, hash, salt).first();
  const userId = created.id;
  await env2.DB.prepare(
    `INSERT INTO profiles (user_id, bio, badges_json)
     VALUES (?, '\u041D\u043E\u0432\u044B\u0439 \u0438\u0433\u0440\u043E\u043A AquaTech.', ?)`
  ).bind(userId, JSON.stringify(["\u041D\u043E\u0432\u0438\u0447\u043E\u043A", "\u0421 \u0441\u0430\u0439\u0442\u0430"])).run();
  const sid = newSessionId();
  await env2.DB.prepare("INSERT INTO sessions (id, user_id, expires_at) VALUES (?, ?, ?)").bind(sid, userId, sessionExpiryIso()).run();
  return json(
    { ok: true, user: { nick } },
    201,
    { "set-cookie": sessionCookie(sid) }
  );
}
__name(onRequestPost11, "onRequestPost");

// api/server-status.js
var DEFAULT_HOST = "g-pl-3.apexnodes.xyz";
var DEFAULT_PORT = 21561;
var CACHE_TTL_MS = 3e4;
var memCache = null;
function resolveAddress(env2) {
  const raw = String(env2?.SERVER_ADDRESS || `${DEFAULT_HOST}:${DEFAULT_PORT}`).trim();
  const cleaned = raw.replace(/^https?:\/\//, "");
  const idx = cleaned.lastIndexOf(":");
  if (idx > 0) {
    const host = cleaned.slice(0, idx).trim();
    const port = Number(cleaned.slice(idx + 1)) || DEFAULT_PORT;
    return { host, port, address: `${host}:${port}` };
  }
  return { host: cleaned || DEFAULT_HOST, port: DEFAULT_PORT, address: `${cleaned || DEFAULT_HOST}:${DEFAULT_PORT}` };
}
__name(resolveAddress, "resolveAddress");
async function onRequestGet13(context2) {
  const { host, port, address } = resolveAddress(context2?.env);
  const now = Date.now();
  if (memCache && now - memCache.at < CACHE_TTL_MS && memCache.payload?.address === address) {
    return json({ ok: true, ...memCache.payload, cached: true });
  }
  const mirrors = [
    `https://api.mcstatus.io/v2/status/java/${encodeURIComponent(address)}`,
    `https://api.mcsrvstat.us/3/${encodeURIComponent(address)}`
  ];
  const payload = await Promise.any(
    mirrors.map((url) => fetchStatus(url, host, port, address))
  ).catch(() => ({
    online: false,
    players_online: 0,
    players_max: 0,
    host,
    port,
    address,
    source: "unreachable"
  }));
  memCache = { at: now, payload };
  return json({ ok: true, ...payload, cached: false });
}
__name(onRequestGet13, "onRequestGet");
async function fetchStatus(url, host, port, address) {
  const ctrl = new AbortController();
  const timer = setTimeout(() => ctrl.abort(), 2500);
  try {
    const res = await fetch(url, {
      headers: { Accept: "application/json", "User-Agent": "AquaTechPortal/1.0" },
      signal: ctrl.signal
    });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const data = await res.json();
    const parsed = parseStatus(data, host, port, address);
    if (!parsed) throw new Error("unparsed");
    return parsed;
  } finally {
    clearTimeout(timer);
  }
}
__name(fetchStatus, "fetchStatus");
function parseStatus(data, host, port, address) {
  if (!data || typeof data !== "object") return null;
  if (typeof data.online === "boolean" && data.players) {
    return {
      online: data.online,
      players_online: Number(data.players.online ?? 0) || 0,
      players_max: Number(data.players.max ?? 0) || 0,
      version: data.version?.name_clean || data.version?.name || null,
      host,
      port,
      address,
      source: "mcstatus.io"
    };
  }
  if (typeof data.online === "boolean") {
    return {
      online: data.online,
      players_online: Number(data.players?.online ?? 0) || 0,
      players_max: Number(data.players?.max ?? 0) || 0,
      version: data.version || null,
      host,
      port,
      address,
      source: "mcsrvstat.us"
    };
  }
  return null;
}
__name(parseStatus, "parseStatus");

// api/site.js
async function onRequestGet14(context2) {
  const { env: env2, request } = context2;
  if (!env2.DB) return bad("\u0411\u0430\u0437\u0430 \u043D\u0435 \u043F\u043E\u0434\u043A\u043B\u044E\u0447\u0435\u043D\u0430", 503);
  const url = new URL(request.url);
  const withNews = url.searchParams.get("news") !== "0";
  try {
    const copy = await getSiteCopy(env2.DB);
    const news = withNews ? await listNews(env2.DB, { publishedOnly: true, limit: 6 }) : void 0;
    return json({ ok: true, copy, ...news ? { news } : {} });
  } catch (err) {
    return bad(err?.message || "\u041D\u0435 \u0443\u0434\u0430\u043B\u043E\u0441\u044C \u0437\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044C \u0441\u0430\u0439\u0442", 500);
  }
}
__name(onRequestGet14, "onRequestGet");

// ../.wrangler/tmp/pages-ySIcaN/functionsRoutes-0.008552840561152353.mjs
var routes = [
  {
    routePath: "/api/admin/catalog/:id",
    mountPath: "/api/admin/catalog",
    method: "PATCH",
    middlewares: [],
    modules: [onRequestPatch]
  },
  {
    routePath: "/api/admin/news/:id",
    mountPath: "/api/admin/news",
    method: "DELETE",
    middlewares: [],
    modules: [onRequestDelete]
  },
  {
    routePath: "/api/admin/news/:id",
    mountPath: "/api/admin/news",
    method: "PATCH",
    middlewares: [],
    modules: [onRequestPatch2]
  },
  {
    routePath: "/api/admin/users/:nick",
    mountPath: "/api/admin/users",
    method: "PATCH",
    middlewares: [],
    modules: [onRequestPatch3]
  },
  {
    routePath: "/api/profiles/:nick/like",
    mountPath: "/api/profiles/:nick",
    method: "POST",
    middlewares: [],
    modules: [onRequestPost]
  },
  {
    routePath: "/api/admin/catalog",
    mountPath: "/api/admin",
    method: "GET",
    middlewares: [],
    modules: [onRequestGet]
  },
  {
    routePath: "/api/admin/catalog",
    mountPath: "/api/admin",
    method: "POST",
    middlewares: [],
    modules: [onRequestPost2]
  },
  {
    routePath: "/api/admin/me",
    mountPath: "/api/admin",
    method: "GET",
    middlewares: [],
    modules: [onRequestGet2]
  },
  {
    routePath: "/api/admin/news",
    mountPath: "/api/admin",
    method: "GET",
    middlewares: [],
    modules: [onRequestGet3]
  },
  {
    routePath: "/api/admin/news",
    mountPath: "/api/admin",
    method: "POST",
    middlewares: [],
    modules: [onRequestPost3]
  },
  {
    routePath: "/api/admin/settings",
    mountPath: "/api/admin",
    method: "GET",
    middlewares: [],
    modules: [onRequestGet4]
  },
  {
    routePath: "/api/admin/settings",
    mountPath: "/api/admin",
    method: "PATCH",
    middlewares: [],
    modules: [onRequestPatch4]
  },
  {
    routePath: "/api/admin/users",
    mountPath: "/api/admin",
    method: "GET",
    middlewares: [],
    modules: [onRequestGet5]
  },
  {
    routePath: "/api/launcher/ensure-nick",
    mountPath: "/api/launcher",
    method: "POST",
    middlewares: [],
    modules: [onRequestPost4]
  },
  {
    routePath: "/api/launcher/session",
    mountPath: "/api/launcher",
    method: "GET",
    middlewares: [],
    modules: [onRequestGet6]
  },
  {
    routePath: "/api/launcher/session",
    mountPath: "/api/launcher",
    method: "POST",
    middlewares: [],
    modules: [onRequestPost5]
  },
  {
    routePath: "/api/launcher/verify-token",
    mountPath: "/api/launcher",
    method: "POST",
    middlewares: [],
    modules: [onRequestPost6]
  },
  {
    routePath: "/api/sync/player",
    mountPath: "/api/sync",
    method: "POST",
    middlewares: [],
    modules: [onRequestPost7]
  },
  {
    routePath: "/api/profiles/:nick",
    mountPath: "/api/profiles",
    method: "GET",
    middlewares: [],
    modules: [onRequestGet7]
  },
  {
    routePath: "/api/profiles/:nick",
    mountPath: "/api/profiles",
    method: "PATCH",
    middlewares: [],
    modules: [onRequestPatch5]
  },
  {
    routePath: "/api/catalog",
    mountPath: "/api",
    method: "GET",
    middlewares: [],
    modules: [onRequestGet8]
  },
  {
    routePath: "/api/login",
    mountPath: "/api",
    method: "POST",
    middlewares: [],
    modules: [onRequestPost8]
  },
  {
    routePath: "/api/logout",
    mountPath: "/api",
    method: "POST",
    middlewares: [],
    modules: [onRequestPost9]
  },
  {
    routePath: "/api/me",
    mountPath: "/api",
    method: "GET",
    middlewares: [],
    modules: [onRequestGet9]
  },
  {
    routePath: "/api/news",
    mountPath: "/api",
    method: "GET",
    middlewares: [],
    modules: [onRequestGet10]
  },
  {
    routePath: "/api/players",
    mountPath: "/api",
    method: "GET",
    middlewares: [],
    modules: [onRequestGet11]
  },
  {
    routePath: "/api/purchase",
    mountPath: "/api",
    method: "GET",
    middlewares: [],
    modules: [onRequestGet12]
  },
  {
    routePath: "/api/purchase",
    mountPath: "/api",
    method: "POST",
    middlewares: [],
    modules: [onRequestPost10]
  },
  {
    routePath: "/api/register",
    mountPath: "/api",
    method: "POST",
    middlewares: [],
    modules: [onRequestPost11]
  },
  {
    routePath: "/api/server-status",
    mountPath: "/api",
    method: "GET",
    middlewares: [],
    modules: [onRequestGet13]
  },
  {
    routePath: "/api/site",
    mountPath: "/api",
    method: "GET",
    middlewares: [],
    modules: [onRequestGet14]
  }
];

// ../tools/tools_npm/node_modules/path-to-regexp/dist.es2015/index.js
function lexer(str) {
  var tokens = [];
  var i = 0;
  while (i < str.length) {
    var char = str[i];
    if (char === "*" || char === "+" || char === "?") {
      tokens.push({ type: "MODIFIER", index: i, value: str[i++] });
      continue;
    }
    if (char === "\\") {
      tokens.push({ type: "ESCAPED_CHAR", index: i++, value: str[i++] });
      continue;
    }
    if (char === "{") {
      tokens.push({ type: "OPEN", index: i, value: str[i++] });
      continue;
    }
    if (char === "}") {
      tokens.push({ type: "CLOSE", index: i, value: str[i++] });
      continue;
    }
    if (char === ":") {
      var name = "";
      var j = i + 1;
      while (j < str.length) {
        var code = str.charCodeAt(j);
        if (
          // `0-9`
          code >= 48 && code <= 57 || // `A-Z`
          code >= 65 && code <= 90 || // `a-z`
          code >= 97 && code <= 122 || // `_`
          code === 95
        ) {
          name += str[j++];
          continue;
        }
        break;
      }
      if (!name)
        throw new TypeError("Missing parameter name at ".concat(i));
      tokens.push({ type: "NAME", index: i, value: name });
      i = j;
      continue;
    }
    if (char === "(") {
      var count3 = 1;
      var pattern = "";
      var j = i + 1;
      if (str[j] === "?") {
        throw new TypeError('Pattern cannot start with "?" at '.concat(j));
      }
      while (j < str.length) {
        if (str[j] === "\\") {
          pattern += str[j++] + str[j++];
          continue;
        }
        if (str[j] === ")") {
          count3--;
          if (count3 === 0) {
            j++;
            break;
          }
        } else if (str[j] === "(") {
          count3++;
          if (str[j + 1] !== "?") {
            throw new TypeError("Capturing groups are not allowed at ".concat(j));
          }
        }
        pattern += str[j++];
      }
      if (count3)
        throw new TypeError("Unbalanced pattern at ".concat(i));
      if (!pattern)
        throw new TypeError("Missing pattern at ".concat(i));
      tokens.push({ type: "PATTERN", index: i, value: pattern });
      i = j;
      continue;
    }
    tokens.push({ type: "CHAR", index: i, value: str[i++] });
  }
  tokens.push({ type: "END", index: i, value: "" });
  return tokens;
}
__name(lexer, "lexer");
function parse(str, options) {
  if (options === void 0) {
    options = {};
  }
  var tokens = lexer(str);
  var _a = options.prefixes, prefixes = _a === void 0 ? "./" : _a, _b = options.delimiter, delimiter = _b === void 0 ? "/#?" : _b;
  var result = [];
  var key = 0;
  var i = 0;
  var path = "";
  var tryConsume = /* @__PURE__ */ __name(function(type) {
    if (i < tokens.length && tokens[i].type === type)
      return tokens[i++].value;
  }, "tryConsume");
  var mustConsume = /* @__PURE__ */ __name(function(type) {
    var value2 = tryConsume(type);
    if (value2 !== void 0)
      return value2;
    var _a2 = tokens[i], nextType = _a2.type, index = _a2.index;
    throw new TypeError("Unexpected ".concat(nextType, " at ").concat(index, ", expected ").concat(type));
  }, "mustConsume");
  var consumeText = /* @__PURE__ */ __name(function() {
    var result2 = "";
    var value2;
    while (value2 = tryConsume("CHAR") || tryConsume("ESCAPED_CHAR")) {
      result2 += value2;
    }
    return result2;
  }, "consumeText");
  var isSafe = /* @__PURE__ */ __name(function(value2) {
    for (var _i = 0, delimiter_1 = delimiter; _i < delimiter_1.length; _i++) {
      var char2 = delimiter_1[_i];
      if (value2.indexOf(char2) > -1)
        return true;
    }
    return false;
  }, "isSafe");
  var safePattern = /* @__PURE__ */ __name(function(prefix2) {
    var prev = result[result.length - 1];
    var prevText = prefix2 || (prev && typeof prev === "string" ? prev : "");
    if (prev && !prevText) {
      throw new TypeError('Must have text between two parameters, missing text after "'.concat(prev.name, '"'));
    }
    if (!prevText || isSafe(prevText))
      return "[^".concat(escapeString(delimiter), "]+?");
    return "(?:(?!".concat(escapeString(prevText), ")[^").concat(escapeString(delimiter), "])+?");
  }, "safePattern");
  while (i < tokens.length) {
    var char = tryConsume("CHAR");
    var name = tryConsume("NAME");
    var pattern = tryConsume("PATTERN");
    if (name || pattern) {
      var prefix = char || "";
      if (prefixes.indexOf(prefix) === -1) {
        path += prefix;
        prefix = "";
      }
      if (path) {
        result.push(path);
        path = "";
      }
      result.push({
        name: name || key++,
        prefix,
        suffix: "",
        pattern: pattern || safePattern(prefix),
        modifier: tryConsume("MODIFIER") || ""
      });
      continue;
    }
    var value = char || tryConsume("ESCAPED_CHAR");
    if (value) {
      path += value;
      continue;
    }
    if (path) {
      result.push(path);
      path = "";
    }
    var open = tryConsume("OPEN");
    if (open) {
      var prefix = consumeText();
      var name_1 = tryConsume("NAME") || "";
      var pattern_1 = tryConsume("PATTERN") || "";
      var suffix = consumeText();
      mustConsume("CLOSE");
      result.push({
        name: name_1 || (pattern_1 ? key++ : ""),
        pattern: name_1 && !pattern_1 ? safePattern(prefix) : pattern_1,
        prefix,
        suffix,
        modifier: tryConsume("MODIFIER") || ""
      });
      continue;
    }
    mustConsume("END");
  }
  return result;
}
__name(parse, "parse");
function match(str, options) {
  var keys = [];
  var re = pathToRegexp(str, keys, options);
  return regexpToFunction(re, keys, options);
}
__name(match, "match");
function regexpToFunction(re, keys, options) {
  if (options === void 0) {
    options = {};
  }
  var _a = options.decode, decode = _a === void 0 ? function(x) {
    return x;
  } : _a;
  return function(pathname) {
    var m = re.exec(pathname);
    if (!m)
      return false;
    var path = m[0], index = m.index;
    var params = /* @__PURE__ */ Object.create(null);
    var _loop_1 = /* @__PURE__ */ __name(function(i2) {
      if (m[i2] === void 0)
        return "continue";
      var key = keys[i2 - 1];
      if (key.modifier === "*" || key.modifier === "+") {
        params[key.name] = m[i2].split(key.prefix + key.suffix).map(function(value) {
          return decode(value, key);
        });
      } else {
        params[key.name] = decode(m[i2], key);
      }
    }, "_loop_1");
    for (var i = 1; i < m.length; i++) {
      _loop_1(i);
    }
    return { path, index, params };
  };
}
__name(regexpToFunction, "regexpToFunction");
function escapeString(str) {
  return str.replace(/([.+*?=^!:${}()[\]|/\\])/g, "\\$1");
}
__name(escapeString, "escapeString");
function flags(options) {
  return options && options.sensitive ? "" : "i";
}
__name(flags, "flags");
function regexpToRegexp(path, keys) {
  if (!keys)
    return path;
  var groupsRegex = /\((?:\?<(.*?)>)?(?!\?)/g;
  var index = 0;
  var execResult = groupsRegex.exec(path.source);
  while (execResult) {
    keys.push({
      // Use parenthesized substring match if available, index otherwise
      name: execResult[1] || index++,
      prefix: "",
      suffix: "",
      modifier: "",
      pattern: ""
    });
    execResult = groupsRegex.exec(path.source);
  }
  return path;
}
__name(regexpToRegexp, "regexpToRegexp");
function arrayToRegexp(paths, keys, options) {
  var parts = paths.map(function(path) {
    return pathToRegexp(path, keys, options).source;
  });
  return new RegExp("(?:".concat(parts.join("|"), ")"), flags(options));
}
__name(arrayToRegexp, "arrayToRegexp");
function stringToRegexp(path, keys, options) {
  return tokensToRegexp(parse(path, options), keys, options);
}
__name(stringToRegexp, "stringToRegexp");
function tokensToRegexp(tokens, keys, options) {
  if (options === void 0) {
    options = {};
  }
  var _a = options.strict, strict = _a === void 0 ? false : _a, _b = options.start, start = _b === void 0 ? true : _b, _c = options.end, end = _c === void 0 ? true : _c, _d = options.encode, encode = _d === void 0 ? function(x) {
    return x;
  } : _d, _e = options.delimiter, delimiter = _e === void 0 ? "/#?" : _e, _f = options.endsWith, endsWith = _f === void 0 ? "" : _f;
  var endsWithRe = "[".concat(escapeString(endsWith), "]|$");
  var delimiterRe = "[".concat(escapeString(delimiter), "]");
  var route = start ? "^" : "";
  for (var _i = 0, tokens_1 = tokens; _i < tokens_1.length; _i++) {
    var token = tokens_1[_i];
    if (typeof token === "string") {
      route += escapeString(encode(token));
    } else {
      var prefix = escapeString(encode(token.prefix));
      var suffix = escapeString(encode(token.suffix));
      if (token.pattern) {
        if (keys)
          keys.push(token);
        if (prefix || suffix) {
          if (token.modifier === "+" || token.modifier === "*") {
            var mod = token.modifier === "*" ? "?" : "";
            route += "(?:".concat(prefix, "((?:").concat(token.pattern, ")(?:").concat(suffix).concat(prefix, "(?:").concat(token.pattern, "))*)").concat(suffix, ")").concat(mod);
          } else {
            route += "(?:".concat(prefix, "(").concat(token.pattern, ")").concat(suffix, ")").concat(token.modifier);
          }
        } else {
          if (token.modifier === "+" || token.modifier === "*") {
            throw new TypeError('Can not repeat "'.concat(token.name, '" without a prefix and suffix'));
          }
          route += "(".concat(token.pattern, ")").concat(token.modifier);
        }
      } else {
        route += "(?:".concat(prefix).concat(suffix, ")").concat(token.modifier);
      }
    }
  }
  if (end) {
    if (!strict)
      route += "".concat(delimiterRe, "?");
    route += !options.endsWith ? "$" : "(?=".concat(endsWithRe, ")");
  } else {
    var endToken = tokens[tokens.length - 1];
    var isEndDelimited = typeof endToken === "string" ? delimiterRe.indexOf(endToken[endToken.length - 1]) > -1 : endToken === void 0;
    if (!strict) {
      route += "(?:".concat(delimiterRe, "(?=").concat(endsWithRe, "))?");
    }
    if (!isEndDelimited) {
      route += "(?=".concat(delimiterRe, "|").concat(endsWithRe, ")");
    }
  }
  return new RegExp(route, flags(options));
}
__name(tokensToRegexp, "tokensToRegexp");
function pathToRegexp(path, keys, options) {
  if (path instanceof RegExp)
    return regexpToRegexp(path, keys);
  if (Array.isArray(path))
    return arrayToRegexp(path, keys, options);
  return stringToRegexp(path, keys, options);
}
__name(pathToRegexp, "pathToRegexp");

// ../tools/tools_npm/node_modules/wrangler/templates/pages-template-worker.ts
var escapeRegex = /[.+?^${}()|[\]\\]/g;
function* executeRequest(request) {
  const requestPath = new URL(request.url).pathname;
  for (const route of [...routes].reverse()) {
    if (route.method && route.method !== request.method) {
      continue;
    }
    const routeMatcher = match(route.routePath.replace(escapeRegex, "\\$&"), {
      end: false
    });
    const mountMatcher = match(route.mountPath.replace(escapeRegex, "\\$&"), {
      end: false
    });
    const matchResult = routeMatcher(requestPath);
    const mountMatchResult = mountMatcher(requestPath);
    if (matchResult && mountMatchResult) {
      for (const handler of route.middlewares.flat()) {
        yield {
          handler,
          params: matchResult.params,
          path: mountMatchResult.path
        };
      }
    }
  }
  for (const route of routes) {
    if (route.method && route.method !== request.method) {
      continue;
    }
    const routeMatcher = match(route.routePath.replace(escapeRegex, "\\$&"), {
      end: true
    });
    const mountMatcher = match(route.mountPath.replace(escapeRegex, "\\$&"), {
      end: false
    });
    const matchResult = routeMatcher(requestPath);
    const mountMatchResult = mountMatcher(requestPath);
    if (matchResult && mountMatchResult && route.modules.length) {
      for (const handler of route.modules.flat()) {
        yield {
          handler,
          params: matchResult.params,
          path: matchResult.path
        };
      }
      break;
    }
  }
}
__name(executeRequest, "executeRequest");
var pages_template_worker_default = {
  async fetch(originalRequest, env2, workerContext) {
    let request = originalRequest;
    const handlerIterator = executeRequest(request);
    let data = {};
    let isFailOpen = false;
    const next = /* @__PURE__ */ __name(async (input, init) => {
      if (input !== void 0) {
        let url = input;
        if (typeof input === "string") {
          url = new URL(input, request.url).toString();
        }
        request = new Request(url, init);
      }
      const result = handlerIterator.next();
      if (result.done === false) {
        const { handler, params, path } = result.value;
        const context2 = {
          request: new Request(request.clone()),
          functionPath: path,
          next,
          params,
          get data() {
            return data;
          },
          set data(value) {
            if (typeof value !== "object" || value === null) {
              throw new Error("context.data must be an object");
            }
            data = value;
          },
          env: env2,
          waitUntil: workerContext.waitUntil.bind(workerContext),
          passThroughOnException: /* @__PURE__ */ __name(() => {
            isFailOpen = true;
          }, "passThroughOnException")
        };
        const response = await handler(context2);
        if (!(response instanceof Response)) {
          throw new Error("Your Pages function should return a Response");
        }
        return cloneResponse(response);
      } else if ("ASSETS") {
        const response = await env2["ASSETS"].fetch(request);
        return cloneResponse(response);
      } else {
        const response = await fetch(request);
        return cloneResponse(response);
      }
    }, "next");
    try {
      return await next();
    } catch (error3) {
      if (isFailOpen) {
        const response = await env2["ASSETS"].fetch(request);
        return cloneResponse(response);
      }
      throw error3;
    }
  }
};
var cloneResponse = /* @__PURE__ */ __name((response) => (
  // https://fetch.spec.whatwg.org/#null-body-status
  new Response(
    [101, 204, 205, 304].includes(response.status) ? null : response.body,
    response
  )
), "cloneResponse");
export {
  pages_template_worker_default as default
};
