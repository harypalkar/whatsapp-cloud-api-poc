import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DemoApiService } from '../../core/services/demo-api.service';
import { unwrapPage } from '../../core/utils/page.util';

interface LocalMsg {
  id: string;
  direction: string;
  body: string;
  deliveryStatus?: string;
  type?: string;
  local?: boolean;
  note?: boolean;
  mediaUrl?: string;
  createdAt?: string;
}

@Component({
  selector: 'wf-inbox',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './inbox.component.html',
  styleUrl: './inbox.component.scss',
})
export class InboxComponent implements OnInit {
  loadingList = signal(true);
  loadingMsgs = signal(false);
  conversations = signal<any[]>([]);
  selectedId = signal<string | null>(null);
  messages = signal<LocalMsg[]>([]);
  customerMap = signal<Record<string, any>>({});
  reply = '';
  note = '';
  filter = '';
  agent = 'Dr Rajesh Sharma';
  typing = signal(false);
  showEmoji = signal(false);

  agents = ['Dr Rajesh Sharma', 'Nurse Priya Nair', 'Front Desk Amit', 'Billing Neha'];
  emojis = ['🙂', '🙏', '✅', '📅', '💊', '🏥', '👍', '❤️'];

  selected = computed(() => this.conversations().find((c) => c.id === this.selectedId()) || null);

  filtered(): any[] {
    const q = this.filter.trim().toLowerCase();
    if (!q) return this.conversations();
    return this.conversations().filter((c) => {
      const name = (this.customerName(c) || '').toLowerCase();
      const preview = (c.lastMessagePreview || '').toLowerCase();
      return name.includes(q) || preview.includes(q);
    });
  }

  constructor(private demo: DemoApiService) {}

  ngOnInit() {
    this.demo.customers(0, 200).subscribe({
      next: (data) => {
        const map: Record<string, any> = {};
        unwrapPage(data).content.forEach((c: any) => { map[c.id] = c; });
        this.customerMap.set(map);
      },
    });
    this.demo.conversations().subscribe({
      next: (data) => {
        const list = unwrapPage(data).content;
        this.conversations.set(list);
        this.loadingList.set(false);
        if (list.length) this.select(list[0].id);
      },
      error: () => this.loadingList.set(false),
    });
  }

  select(id: string) {
    this.selectedId.set(id);
    this.loadingMsgs.set(true);
    this.typing.set(false);
    this.messages.set([]);
    this.demo.messages(id).subscribe({
      next: (data) => {
        const msgs = unwrapPage<LocalMsg>(data).content.slice().reverse().map((m, idx) => {
          if (idx === 0 && !m.mediaUrl && (m.body || '').toLowerCase().includes('pdf')) {
            return { ...m, type: 'document', mediaUrl: 'Admission Brochure.pdf' };
          }
          return m;
        });
        this.messages.set(msgs);
        this.loadingMsgs.set(false);
        this.typing.set(true);
        setTimeout(() => this.typing.set(false), 2200);
      },
      error: () => this.loadingMsgs.set(false),
    });
  }

  customerName(c: any): string {
    if (!c) return 'Unknown';
    return this.customerMap()[c.customerId]?.name || `Chat ${String(c.id || '').slice(0, 8)}`;
  }

  customerMobile(c: any): string {
    return this.customerMap()[c?.customerId]?.mobileE164 || '';
  }

  statusLabel(s?: string): string {
    const v = (s || '').toUpperCase();
    if (v === 'READ') return 'Read';
    if (v === 'DELIVERED') return 'Delivered';
    if (v === 'SENT') return 'Sent';
    if (v === 'FAILED') return 'Failed';
    return v || '';
  }

  insertEmoji(e: string) {
    this.reply += e;
    this.showEmoji.set(false);
  }

  sendReply() {
    const text = this.reply.trim();
    if (!text || !this.selectedId()) return;
    const msg: LocalMsg = {
      id: 'local-' + Date.now(),
      direction: 'OUT',
      body: text,
      deliveryStatus: 'SENT',
      type: 'text',
      local: true,
    };
    this.messages.update((m) => [...m, msg]);
    this.reply = '';
    setTimeout(() => {
      this.messages.update((m) =>
        m.map((x) => x.id === msg.id ? { ...x, deliveryStatus: 'DELIVERED' } : x),
      );
    }, 700);
    setTimeout(() => {
      this.messages.update((m) =>
        m.map((x) => x.id === msg.id ? { ...x, deliveryStatus: 'READ' } : x),
      );
    }, 1600);
  }

  addNote() {
    const text = this.note.trim();
    if (!text || !this.selectedId()) return;
    const msg: LocalMsg = {
      id: 'note-' + Date.now(),
      direction: 'NOTE',
      body: text,
      note: true,
      local: true,
    };
    this.messages.update((m) => [...m, msg]);
    this.note = '';
  }

  isOut(m: LocalMsg): boolean {
    const d = (m.direction || '').toUpperCase();
    return d === 'OUT' || d === 'OUTBOUND' || d.startsWith('OUT');
  }

  isMedia(m: LocalMsg): boolean {
    const t = (m.type || '').toLowerCase();
    return !!m.mediaUrl || t === 'image' || t === 'document' || t === 'video';
  }
}
