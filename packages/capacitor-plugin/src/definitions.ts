export interface CapacitorIntentsPlugin {
  registerBroadcastReceiver(
    options: { filters: string[] },
    callback: (data: { [key: string]: any }) => void
  ): Promise<string>;

  unregisterBroadcastReceiver(options: { id: string }): Promise<void>;

  getDeviceInfo(): Promise<{ hasBarcodeScanner: boolean }>;

  sendBroadcastIntent(options: { action: string; value: { [key: string]: any } }): Promise<void>;
}
