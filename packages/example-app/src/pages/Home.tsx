import { IonContent, IonHeader, IonPage, IonTitle, IonToolbar, IonButton } from '@ionic/react';
import './Home.css';

import { CapacitorIntents } from '@nanta/capacitor-intents';
import { useCallback, useState } from 'react';

const Home: React.FC = () => {
  const [, updateState] = useState<any>();
  const forceUpdate = useCallback(() => updateState({}), []);

  let receiverId: string | null = null;
  const [isTestSetup, setIsTestSetup] = useState<boolean>(false);
  const [testState, setTestState] = useState<boolean | null>(null);
  const [deviceInfo, setDeviceInfo] = useState<string | null>(null);


  const setupPluginTest = async () => {
    const rId = await CapacitorIntents.registerBroadcastReceiver({ filters: ['example.itmikes.action'] }, async (data) => {
      // data is a JS Object but could contain any structure
      console.dir(data);
      const extras = JSON.parse(data['extras'].value);
      console.dir(extras);
      console.log(receiverId);

      if (extras['testValue'] === "Test String" && receiverId !== null) {
        setTestState(true);
      } else {
        setTestState(false);
      }

      // now unregister
      if (receiverId !== null) {
        await CapacitorIntents.unregisterBroadcastReceiver({ id: receiverId });
        receiverId = null;
      }
      forceUpdate();
    });

    receiverId = `${rId}`;
    setTestState(null);
    setIsTestSetup(true);
  }

  const testPlugin = async () => {
    await CapacitorIntents.sendBroadcastIntent({ action: 'example.itmikes.action', value: { testValue: "Test String" } });
    setIsTestSetup(false);
  }

  const testHasScanner = async () => {
    const res = await CapacitorIntents.getDeviceInfo();
    console.log('------test result ------')
    console.log(JSON.stringify(res))
    setDeviceInfo(JSON.stringify(res));
    console.log(res?.hasBarcodeScanner);
    console.log('------test result ------')

  }

  return (
    <IonPage>
      <IonHeader>
        <IonToolbar>
          <IonTitle>Blank</IonTitle>
        </IonToolbar>
      </IonHeader>
      <IonContent fullscreen>
        <IonHeader collapse="condense">
          <IonToolbar>
            <IonTitle size="large">Blank</IonTitle>
          </IonToolbar>
        </IonHeader>
        <div>
          <IonButton disabled={isTestSetup} onClick={setupPluginTest}>Setup Test</IonButton>
          <IonButton disabled={!isTestSetup} onClick={testPlugin}>Run Test</IonButton>
          <IonButton onClick={testHasScanner}>Has Scanner</IonButton>

          <p><span style={{ fontWeight: 'bolder' }}>Test Passed:</span> {testState === null ? "Not Yet Run" : (testState ? "Passed" : "Failed")}</p>
          <p>{deviceInfo}</p>
        </div>
      </IonContent>
    </IonPage>
  );
};

export default Home;
