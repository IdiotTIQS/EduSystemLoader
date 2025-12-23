import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import './style.css';

// FontAwesome
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import { library } from '@fortawesome/fontawesome-svg-core';
import { faUser, faLock, faEnvelope, faGraduationCap, faIdCard, faPhone } from '@fortawesome/free-solid-svg-icons';
import { faQq, faWeixin, faWeibo, faAlipay } from '@fortawesome/free-brands-svg-icons';

library.add(faUser, faLock, faEnvelope, faGraduationCap, faIdCard, faPhone, faQq, faWeixin, faWeibo, faAlipay);

const app = createApp(App);
app.component('font-awesome-icon', FontAwesomeIcon);
app.use(router);
app.mount('#app');
