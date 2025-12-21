import axios from 'axios';
import config from "../config/index.js";

export const api = axios.create({
  baseURL: config.api.baseURL,
  timeout: 20000
});