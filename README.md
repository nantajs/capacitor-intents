# @nanta/capacitor-intents

Simple intent tools for Capacitor on Android platform.

Origin fork from [capacitor-intents](https://github.com/IT-MikeS/capacitor-intents)。
This fork mainly support android 14+ platform.

<a href="https://www.npmjs.com/package/@nanta/capacitor-intents"><img src="https://badgen.net/npm/dm/@nanta/capacitor-intents" alt="Download"></a>
<a href="https://www.npmjs.com/package/@nanta/capacitor-intents"><img src="https://badgen.net/npm/v/@nanta/capacitor-intents" alt="Version"></a>
<a href="https://www.npmjs.com/package/@nanta/capacitor-intents"><img src="https://badgen.net/npm/license/@nanta/capacitor-intents" alt="License"></a> 

## Install

```bash
npm install @nanta/capacitor-intents
npx cap sync
```

## Usage

See `example-app` in `packages` folder.

## API

<docgen-index>

* [`registerBroadcastReceiver(...)`](#registerbroadcastreceiver)
* [`unregisterBroadcastReceiver(...)`](#unregisterbroadcastreceiver)
* [`sendBroadcastIntent(...)`](#sendbroadcastintent)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### registerBroadcastReceiver(...)

```typescript
registerBroadcastReceiver(options: { filters: string[]; }, callback: (data: { [key: string]: any; }) => void) => any
```

| Param          | Type                                                    |
| -------------- | ------------------------------------------------------- |
| **`options`**  | <code>{ filters: {}; }</code>                           |
| **`callback`** | <code>(data: { [key: string]: any; }) =&gt; void</code> |

**Returns:** <code>any</code>

--------------------


### unregisterBroadcastReceiver(...)

```typescript
unregisterBroadcastReceiver(options: { id: string; }) => any
```

| Param         | Type                         |
| ------------- | ---------------------------- |
| **`options`** | <code>{ id: string; }</code> |

**Returns:** <code>any</code>

--------------------


### sendBroadcastIntent(...)

```typescript
sendBroadcastIntent(options: { action: string; value: { [key: string]: any; }; }) => any
```

| Param         | Type                                                             |
| ------------- | ---------------------------------------------------------------- |
| **`options`** | <code>{ action: string; value: { [key: string]: any; }; }</code> |

**Returns:** <code>any</code>

--------------------

</docgen-api>
